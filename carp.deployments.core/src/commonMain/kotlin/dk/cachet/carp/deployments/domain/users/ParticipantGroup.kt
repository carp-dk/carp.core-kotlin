package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.hasNoConflicts
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.common.domain.users.Account
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A group of participants participating in a study deployment using the [assignedPrimaryDevices].
 * Consent and participant data is managed here.
 *
 * TODO: Implement consent.
 */
class ParticipantGroup private constructor(
    val studyDeploymentId: UUID,
    assignedPrimaryDevices: Set<AssignedPrimaryDevice>,
    val expectedData: Set<ExpectedParticipantData>,
    id: UUID = UUID.randomUUID(),
    createdOn: Instant = Clock.System.now()
) : AggregateRoot<ParticipantGroup, ParticipantGroupSnapshot, ParticipantGroup.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class DataSet(
            /**
             * The role name of the participant role for which data was set, or null when common data.
             */
            val participantRoleName: String?,
            val inputDataType: InputDataType,
            val data: Data?
        ) : Event()
        data class ParticipationAdded( val accountParticipation: AccountParticipation ) : Event()
        data class DeviceRegistrationChanged( val assignedPrimaryDevice: AssignedPrimaryDevice ) : Event()
        object StudyDeploymentStopped : Event()
    }


    init { expectedData.hasNoConflicts( exceptionOnConflict = true ) }

    companion object
    {
        /**
         * Initialize a [ParticipantGroup] with default values for a newly created deployment with [studyDeploymentId]
         * which was just created for the specified study [protocol].
         */
        fun fromNewDeployment( studyDeploymentId: UUID, protocol: StudyProtocol ): ParticipantGroup =
            ParticipantGroup(
                studyDeploymentId,
                protocol.primaryDevices.map { AssignedPrimaryDevice( it ) }.toSet(),
                protocol.expectedParticipantData
            )

        /**
         * Initialize a [ParticipantGroup] with default values for a newly created [deployment].
         */
        fun fromNewDeployment( deployment: StudyDeployment ): ParticipantGroup =
            fromNewDeployment( deployment.id, deployment.protocol )

        fun fromSnapshot( snapshot: ParticipantGroupSnapshot ): ParticipantGroup
        {
            val group = ParticipantGroup(
                snapshot.studyDeploymentId,
                snapshot.assignedPrimaryDevices,
                snapshot.expectedData,
                snapshot.id,
                snapshot.createdOn
            )
            group.isStudyDeploymentStopped = snapshot.isStudyDeploymentStopped

            // Add participations.
            snapshot.participations.forEach { p -> group._participations.add( p.copy() ) }

            // Add participant data.
            snapshot.commonData.forEach { (inputDataType, data) ->
                group._commonData[ inputDataType ] = data
            }
            snapshot.roleData.forEach { (role, data) ->
                data.forEach { (inputDataType, data) ->
                    val roleData = requireNotNull( group._roleData[ role ] )
                        { "Invalid participant group snapshot." }
                    roleData[ inputDataType ] = data
                }
            }

            group.wasLoadedFromSnapshot( snapshot )
            return group
        }
    }


    /**
     * The account IDs participating in this study group and the pseudonym IDs assigned to them.
     */
    val participations: Set<AccountParticipation>
        get() = _participations

    private val _participations: MutableSet<AccountParticipation> = mutableSetOf()

    /**
     * Specify that an [account] (invited using [invitation]) participates in this group, identified by [participation],
     * and was requested to use the [assignedPrimaryDevices].
     *
     * @throws IllegalArgumentException if [participation] is already added to this participant group,
     * or if the [participation] details do not match the study deployment of this participant group.
     * @throws IllegalStateException when the study deployment of this participant group has stopped.
     */
    fun addParticipation(
        account: Account,
        invitation: StudyInvitation,
        participation: Participation,
        assignedPrimaryDevices: Set<AnyPrimaryDeviceConfiguration>
    )
    {
        require( studyDeploymentId == participation.studyDeploymentId )
            { "The specified participation details do not match the study deployment of this participant group." }
        require( _participations.none { it.participation.participantId == participation.participantId } )
            { "The specified participant ID is already added to this study deployment." }
        check( !isStudyDeploymentStopped )

        val assignedPrimaryDeviceRoleNames = assignedPrimaryDevices.map { it.roleName }.toSet()
        val accountParticipation = AccountParticipation(
            participation,
            assignedPrimaryDeviceRoleNames,
            account.id,
            invitation
        )
        _participations.add( accountParticipation )
        event( Event.ParticipationAdded( accountParticipation ) )
    }

    /**
     * The assigned primary devices to participants in this group and their device registrations, if any.
     */
    val assignedPrimaryDevices: Set<AssignedPrimaryDevice>
        get() = _assignedPrimaryDevices

    private val _assignedPrimaryDevices: MutableSet<AssignedPrimaryDevice> = assignedPrimaryDevices.toMutableSet()

    /**
     * Return the [AssignedPrimaryDevice] with the specified [roleName].
     *
     * @throws IllegalArgumentException when no assigned device with [roleName] exists for this participant group.
     */
    fun getAssignedPrimaryDevice( roleName: String ) =
        assignedPrimaryDevices.firstOrNull { it.device.roleName == roleName }
            ?: throw IllegalArgumentException(
                "There is no assigned device with role name \"$roleName\" for this participant group."
            )

    /**
     * Update the device [registration] for the given assigned [primaryDevice].
     *
     * @throws IllegalArgumentException when [primaryDevice] is not part of this participant group.
     */
    fun updateDeviceRegistration( primaryDevice: AnyPrimaryDeviceConfiguration, registration: DeviceRegistration? )
    {
        val assignedDevice = _assignedPrimaryDevices.firstOrNull { it.device == primaryDevice }
        requireNotNull( assignedDevice ) { "The passed primary device is not part of this participant group." }

        if ( assignedDevice.registration != registration )
        {
            _assignedPrimaryDevices.remove( assignedDevice )
            val updatedRegistrationDevice = assignedDevice.copy( registration = registration )
            _assignedPrimaryDevices.add( updatedRegistrationDevice )
            event( Event.DeviceRegistrationChanged( updatedRegistrationDevice ) )
        }
    }

    /**
     * Determines whether the study deployment of this participant group has been stopped
     * and no further participations can be added
     */
    var isStudyDeploymentStopped: Boolean = false
        private set

    fun studyDeploymentStopped()
    {
        if ( !isStudyDeploymentStopped )
        {
            isStudyDeploymentStopped = true
            event( Event.StudyDeploymentStopped )
        }
    }

    /**
     * Data related to everyone in the group.
     */
    val commonData: Map<InputDataType, Data?>
        get() = _commonData

    private val _commonData: MutableMap<InputDataType, Data?> = expectedData
        .filter { it.assignedTo is AssignedTo.All }
        .associate { it.inputDataType to null } // All participant data is null by default.
        .toMutableMap()

    /**
     * Data related to a participant role in the group.
     */
    val roleData: List<ParticipantData.RoleData>
        get() = _roleData.map { (role, data) -> ParticipantData.RoleData( role, data.toMap() ) }

    private val _roleData: Map<String, MutableMap<InputDataType, Data?>> = expectedData
        .filter { it.assignedTo is AssignedTo.Roles }
        .flatMap {
            val roles = (it.assignedTo as AssignedTo.Roles).roleNames
            roles.map { role -> role to it.inputDataType }
        }
        .groupBy { it.first } // Group by role.
        .map { (role, roleInput) ->
            // All participant data is null by default
            val data: MutableMap<InputDataType, Data?> =
                roleInput.map { it.second }.associateWith { null }.toMutableMap()
            role to data
        }
        .toMap()

    /**
     * Set [data] that was [inputByParticipantRole] for the given [inputDataType], or unset if [data] is `null`,
     * using [registeredInputDataTypes] to verify whether the data is valid for default input data types.
     *
     * @throws IllegalArgumentException when:
     *   - [inputDataType] is not configured as expected participant data to be [inputByParticipantRole]
     *   - [data] is invalid data for [inputDataType]
     * @return True when data changed; false when data was already set.
     */
    fun setData(
        registeredInputDataTypes: InputDataTypeList,
        inputDataType: InputDataType,
        data: Data?,
        /**
         * The participant role who filled out [data]; null if all participants can set the specified [inputDataType].
         */
        inputByParticipantRole: String? = null
    ): Boolean
    {
        val dataToSet = getExpectedDataOrThrow( registeredInputDataTypes, inputDataType, data, inputByParticipantRole )

        val prevData =
            if ( dataToSet.assignedTo == AssignedTo.All ) _commonData.put( inputDataType, data )
            else
            {
                val roleData = checkNotNull( _roleData[ inputByParticipantRole ] )
                roleData.put( inputDataType, data )
            }

        return ( prevData != data )
            .eventIf( true ) { Event.DataSet( inputByParticipantRole, inputDataType, data ) }
    }

    /**
     * Set [data] that was [inputByParticipantRole] for the participants in this group, or unset it by passing `null`,
     * using [registeredInputDataTypes] to verify whether the data is valid for default input data types.
     *
     * @throws IllegalArgumentException when:
     *   - one or more of the keys in [data] isn't configured as expected participant data to be [inputByParticipantRole]
     *   - one or more of the set [data] isn't valid for the corresponding input data type
     * @return True when any data has changed; false when all [data] was already set.
     */
    fun setData(
        registeredInputDataTypes: InputDataTypeList,
        data: Map<InputDataType, Data?>,
        /**
         * The participant role who filled out [data]; null if all participants can set it.
         */
        inputByParticipantRole: String? = null
    ): Boolean
    {
        // Fail early if any of the data to be set is invalid.
        // TODO: This is checked again in the `setData` call for each individual set data. Can be optimized if needed.
        data.forEach { getExpectedDataOrThrow( registeredInputDataTypes, it.key, it.value, inputByParticipantRole ) }

        return data.entries.fold( false ) { anyDataChanged, element ->
            setData( registeredInputDataTypes, element.key, element.value, inputByParticipantRole ) || anyDataChanged
        }
    }

    /**
     * Returns the matching [ExpectedParticipantData], but only if the data input is valid; throws otherwise.
     */
    private fun getExpectedDataOrThrow(
        registeredInputDataTypes: InputDataTypeList,
        inputDataType: InputDataType,
        data: Data?,
        assignedToParticipantRole: String?
    ): ExpectedParticipantData
    {
        val dataToSet = expectedData
            .filter {
                when ( val assignedTo = it.assignedTo )
                {
                    is AssignedTo.All -> true
                    is AssignedTo.Roles -> assignedToParticipantRole in assignedTo.roleNames
                }
            }
            .firstOrNull { it.inputDataType == inputDataType }
        requireNotNull( dataToSet ) { "The input data type is not assigned to this participant role." }
        require( dataToSet.attribute.isValidData( registeredInputDataTypes, data ) ) { "Invalid data is passed" }

        return dataToSet
    }

    /**
     * Get an immutable snapshot of the current state of this [ParticipantGroup] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): ParticipantGroupSnapshot =
        ParticipantGroupSnapshot.fromParticipantGroup( this, version )
}
