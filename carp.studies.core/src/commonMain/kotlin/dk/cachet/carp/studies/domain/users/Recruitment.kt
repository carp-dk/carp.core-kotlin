package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.domain.AggregateRoot
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.throwIfInvalidInvitations
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupRepresentation
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.application.users.participantIds
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * Represents a set of [participants] recruited for a study identified by [studyId].
 */
@Suppress( "TooManyFunctions" )
class Recruitment( val studyId: UUID, id: UUID = UUID.randomUUID(), createdOn: Instant = Clock.System.now() ) :
    AggregateRoot<Recruitment, RecruitmentSnapshot, Recruitment.Event>( id, createdOn )
{
    sealed class Event : DomainEvent
    {
        data class ParticipantAdded( val participant: Participant ) : Event()
        data class ParticipantGroupAdded(
            val participants: Set<AssignedParticipantRoles>,
            val representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
        ) : Event()

        /**
         * Indicates the participant group with [groupId] was updated.
         * A `null` value means no change; otherwise, the value is set.
         */
        data class ParticipantGroupUpdated(
            val groupId: UUID,
            val participants: Set<AssignedParticipantRoles>? = null,
            val representation: ParticipantGroupRepresentation? = null
        ) : Event()
    }


    companion object
    {
        fun fromSnapshot( snapshot: RecruitmentSnapshot ): Recruitment
        {
            val recruitment = Recruitment( snapshot.studyId, snapshot.id, snapshot.createdOn )
            if ( snapshot.studyProtocol != null && snapshot.invitation != null )
            {
                recruitment.lockInStudy( snapshot.studyProtocol, snapshot.invitation )
            }
            snapshot.participants.forEach { recruitment._participants.add( it ) }
            snapshot.participantGroups.forEach { recruitment._participantGroups[ it.key ] = it.value }

            recruitment.wasLoadedFromSnapshot( snapshot )
            return recruitment
        }
    }


    // We don't expect massive amounts of participants, so storing them within recruitment is fine for now.
    private val _participants: MutableSet<Participant> = mutableSetOf()

    /**
     * The participants which are part of this [Recruitment].
     */
    val participants: Set<Participant>
        get() = _participants.toSet()

    /**
     * Add a [Participant] by the specified [identity].
     * In case a participant with [identity] was already added before, the same [Participant] is returned.
     */
    fun addParticipant( identity: AccountIdentity, id: UUID = UUID.randomUUID() ): Participant
    {
        // Verify whether participant was already added.
        var participant = _participants.firstOrNull { it.accountIdentity == identity }

        // Add new participant in case it was not added before.
        if ( participant == null )
        {
            participant = Participant( identity, id )
            _participants.add( participant )
            event( Event.ParticipantAdded( participant ) )
        }

        return participant
    }

    /**
     * Add a [Participant] by the specified [email].
     * In case a participant with the same [email] was already added before, the same [Participant] is returned.
     */
    fun addParticipant( email: EmailAddress, id: UUID = UUID.randomUUID() ): Participant =
        addParticipant( EmailAccountIdentity( email ), id )

    /**
     * Add a [Participant] by the specified [username].
     * In case a participant with the same [username] was already added before, the same [Participant] is returned.
     */
    fun addParticipant( username: String, id: UUID = UUID.randomUUID() ): Participant =
        addParticipant( UsernameAccountIdentity( username ), id )


    private var studyProtocol: StudyProtocolSnapshot? = null
    private var invitation: StudyInvitation? = null

    /**
     * Lock in the [protocol] which participants in this recruitment can participate in,
     * and the [invitation] which is sent to them once they are deployed.
     */
    fun lockInStudy( protocol: StudyProtocolSnapshot, invitation: StudyInvitation )
    {
        check( getStatus() is RecruitmentStatus.AwaitingStudyToGoLive )

        this.studyProtocol = protocol
        this.invitation = invitation
    }

    /**
     * Get the status (serializable) of this [Recruitment].
     */
    fun getStatus(): RecruitmentStatus
    {
        val protocol = studyProtocol
        val invitation = invitation
        val status =
            if ( protocol != null && invitation != null ) RecruitmentStatus.ReadyForDeployment( protocol, invitation )
            else RecruitmentStatus.AwaitingStudyToGoLive

        return status
    }

    /**
     * Attempt creating [ParticipantInvitation]s for the specified participant [group],
     * or throw exception in case preconditions are violated.
     *
     * @throws IllegalStateException when the study is not yet ready for deployment.
     * @throws IllegalArgumentException when:
     *  - any of the participants specified in [group] does not exist
     *  - [group] is empty
     *  - any of the participant roles specified in [group] are not part of the configured study protocol
     *  - not all primary devices part of the study protocol have been assigned a participant
     *  - not all necessary participant roles part of the study have been assigned a participant
     */
    fun createInvitations(
        group: Set<AssignedParticipantRoles>
    ): Pair<StudyProtocolSnapshot, List<ParticipantInvitation>>
    {
        val status = getStatus()
        check( status is RecruitmentStatus.ReadyForDeployment )
            { "Study is not yet ready to be deployed to participants." }

        // Verify participants.
        val allParticipants = participants.associateBy { it.id }
        require( group.participantIds().all { it in allParticipants } )
            { "One of the specified participants is not part of this study." }

        // Verify whether invitations match the requirements of the protocol.
        val invitations = group.map { toAssign ->
            val participant = allParticipants.getValue( toAssign.participantId )
            ParticipantInvitation(
                participant.id,
                toAssign.assignedRoles,
                participant.accountIdentity,
                status.invitation
            )
        }
        val protocol = status.studyProtocol
        protocol.throwIfInvalidInvitations( invitations )

        return Pair( protocol, invitations )
    }

    /**
     * Per study deployment ID, the group of participants that participates in it.
     */
    val participantGroups: Map<UUID, StagedParticipantGroup>
        get() = _participantGroups

    private val _participantGroups: MutableMap<UUID, StagedParticipantGroup> = mutableMapOf()

    /**
     * Create and add the [participants] with assigned roles to a participant group, and give it a [representation].
     *
     * [ParticipantGroupRepresentation.Default] is used when no [representation] is passed.
     *
     * @throws IllegalArgumentException when:
     *  - one or more of the participants aren't in this recruitment.
     *  - any of the participant roles specified in [participants] are not part of the configured study protocol.
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    fun addParticipantGroup(
        participants: Set<AssignedParticipantRoles>,
        representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default,
        id: UUID = UUID.randomUUID()
    ): StagedParticipantGroup
    {
        val status = getStatus()
        check( status is RecruitmentStatus.ReadyForDeployment ) { "The study is not yet ready for deployment." }

        validateRoleAssignments( status.studyProtocol, participants )

        val group = StagedParticipantGroup( id, representation )
        group.addParticipants( participants )

        _participantGroups[ group.id ] = group
        event( Event.ParticipantGroupAdded( participants, representation ) )

        return group
    }

    /**
     * Update the [participants] and/or [representation] of an existing participant group with the specified [groupId].
     *
     * @throws IllegalArgumentException when:
     *  - the participant group with [groupId] does not exist.
     *  - one or more of the participants aren't in this recruitment.
     *  - any of the participant roles specified in [participants] are not part of the configured study protocol.
     * @throws IllegalStateException when:
     *  - the recruitment is not ready for deployment
     *  - the group has already been deployed and [participants] changes assignments
     */
    fun updateParticipantGroup(
        groupId: UUID,
        participants: Set<AssignedParticipantRoles>? = null,
        representation: ParticipantGroupRepresentation? = null
    )
    {
        val group = requireNotNull( _participantGroups[ groupId ] )
            { "Participant group with ID \"$groupId\" does not exist." }

        val newRoleAssignments = participants != null && group.roleAssignments != participants
        val newRepresentation = representation != null && group.representation != representation
        if ( newRoleAssignments )
        {
            check( !group.isDeployed )
                { "Participant group has already been deployed; participant assignments can no longer be changed." }

            val status = getStatus()
            check( status is RecruitmentStatus.ReadyForDeployment ) { "The study is not yet ready for deployment." }

            validateRoleAssignments( status.studyProtocol, participants )
            group.replaceParticipants( participants )
        }

        if ( newRepresentation ) group.representation = representation
        if ( newRoleAssignments || newRepresentation )
            event(
                Event.ParticipantGroupUpdated(
                    groupId,
                    participants.takeIf { newRoleAssignments },
                    representation.takeIf { newRepresentation }
                )
            )
    }

    /**
     * Get the [ParticipantGroupStatus] for participant groups with [groupIds] in this recruitment.
     * For deployed participant groups, the matching [StudyDeploymentStatus] is retrieved using
     * [getDeploymentStatusList].
     *
     * @throws IllegalArgumentException when:
     * - [groupIds] contains an id which is not part of this recruitment
     * - [getDeploymentStatusList] doesn't return a matching [StudyDeploymentStatus] for each of the requested IDs
     */
    suspend fun getParticipantGroupStatusList(
        groupIds: Set<UUID>,
        getDeploymentStatusList: suspend (Set<UUID>) -> List<StudyDeploymentStatus>
    ): List<ParticipantGroupStatus>
    {
        require( participantGroups.keys.containsAll( groupIds ) )
            { "One of the group IDs a status is requested for isn't part of this recruitment." }

        val (deployedGroups, stagedGroups) = groupIds
            .map { participantGroups.getValue( it ) }
            .partition { it.isDeployed }

        // Get participant group status for staged groups.
        val stagedGroupStatuses = stagedGroups.map { group ->
            ParticipantGroupStatus.Staged(
                id = group.id,
                participants = getParticipantsFor( group ),
                assignedParticipantRoles = group.roleAssignments.toSet(),
                representation = group.representation
            )
        }

        // Get deployment status for deployed participant groups.
        val deployedGroupIds = deployedGroups.map { it.id }.toSet()
        val deploymentStatuses =
            if ( deployedGroupIds.isEmpty() ) emptyMap()
            else getDeploymentStatusList( deployedGroupIds ).associateBy { it.studyDeploymentId }

        // Get participant group status for deployed groups.
        val deployedGroupStatuses = deployedGroups.map { group ->
            val deploymentStatus = requireNotNull( deploymentStatuses[ group.id ] )
                { "No study deployment status returned for the requested ID: \"${group.id}\"." }
            ParticipantGroupStatus.InDeployment.fromDeploymentStatus(
                getParticipantsFor( group ),
                group.roleAssignments.toSet(),
                deploymentStatus,
                group.representation
            )
        }

        return stagedGroupStatuses + deployedGroupStatuses
    }

    /**
     * Get the [ParticipantGroupStatus] for the participant group with [groupId] in this recruitment.
     * If the participant group is deployed, the matching [StudyDeploymentStatus] is retrieved using
     * [getDeploymentStatus].
     *
     * @throws IllegalArgumentException when:
     * - [groupId] is not part of this recruitment
     * - [getDeploymentStatus] returns a [StudyDeploymentStatus] which doesn't match the requested ID
     */
    suspend fun getParticipantGroupStatus(
        groupId: UUID,
        getDeploymentStatus: suspend (UUID) -> StudyDeploymentStatus
    ): ParticipantGroupStatus =
        getParticipantGroupStatusList( setOf( groupId ) )
            { deploymentIds -> listOf( getDeploymentStatus( deploymentIds.single() ) ) }.single()

    /**
     * Get the participants for the participant [group].
     */
    private fun getParticipantsFor( group: StagedParticipantGroup ): Set<Participant> =
        group.participantIds.map { id -> _participants.first { it.id == id } }.toSet()

    /**
     * Get an immutable snapshot of the current state of this [Recruitment] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): RecruitmentSnapshot =
        RecruitmentSnapshot.fromParticipantRecruitment( this, version )

    /**
     * Validate that all participants exist in this recruitment and that all assigned roles are part of the protocol.
     *
     * @throws IllegalArgumentException when:
     *  - one or more of the participants aren't in this recruitment.
     *  - any of the participant roles specified in [participants] are not part of the configured study protocol.
     */
    private fun validateRoleAssignments( protocol: StudyProtocolSnapshot, participants: Set<AssignedParticipantRoles> )
    {
        require( this.participants.map { it.id }.containsAll( participants.participantIds() ) )
            { "One of the participants for which to create a participant group isn't part of this recruitment." }

        val assignedParticipantRoles = participants
            .map { it.assignedRoles }
            .filterIsInstance<AssignedTo.Roles>()
            .flatMap { it.roleNames }
            .toSet()
        val availableRoles = protocol.participantRoles.map { it.role }.toSet()

        assignedParticipantRoles.forEach { assigned ->
            require( assigned in availableRoles )
                { "The assigned participant role \"$assigned\" is not part of the study protocol." }
        }
    }
}
