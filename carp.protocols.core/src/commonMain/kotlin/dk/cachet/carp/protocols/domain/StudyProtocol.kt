package dk.cachet.carp.protocols.domain

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.isPrimary
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl.Control
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.application.users.hasNoConflicts
import dk.cachet.carp.common.domain.DomainEvent
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.configuration.EmptyProtocolDeviceConfiguration
import dk.cachet.carp.protocols.domain.configuration.EmptyProtocolParticipantConfiguration
import dk.cachet.carp.protocols.domain.configuration.EmptyProtocolTaskConfiguration
import dk.cachet.carp.protocols.domain.configuration.ProtocolParticipantConfiguration
import dk.cachet.carp.protocols.domain.configuration.StudyProtocolComposition
import dk.cachet.carp.protocols.domain.deployment.*
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A description of how a study is to be executed, defining the type(s) of primary device(s) ([AnyPrimaryDeviceConfiguration]) responsible for aggregating data,
 * the optional devices ([AnyDeviceConfiguration]) connected to them, and the [TriggerConfiguration]'s which lead to data collection on said devices.
 */
// TODO: some of the device and task configuration methods are overridden solely to add events. Can this be refactored?
@Suppress( "TooManyFunctions" )
class StudyProtocol(
    /**
     * The entity (e.g., person or group) that created this [StudyProtocol].
     */
    val ownerId: UUID,
    /**
     * A unique descriptive name for the protocol assigned by the protocol owner.
     */
    name: String,
    /**
     * An optional description for the study protocol.
     */
    description: String? = null,
    id: UUID = UUID.randomUUID(),
    createdOn: Instant = Clock.System.now()
) : StudyProtocolComposition(
        EmptyProtocolDeviceConfiguration(),
        EmptyProtocolTaskConfiguration(),
        EmptyProtocolParticipantConfiguration(),
        id,
        createdOn
    )
{
    sealed class Event : DomainEvent
    {
        data class NameChanged( val name: String ) : Event()
        data class DescriptionChanged( val description: String? ) : Event()
        data class PrimaryDeviceAdded( val device: AnyPrimaryDeviceConfiguration ) : Event()
        data class ConnectedDeviceAdded(
            val connected: AnyDeviceConfiguration,
            val primary: AnyPrimaryDeviceConfiguration
        ) : Event()
        data class TriggerAdded( val trigger: TriggerConfiguration<*> ) : Event()
        data class TaskAdded( val task: TaskConfiguration<*> ) : Event()
        data class TaskRemoved( val task: TaskConfiguration<*> ) : Event()
        data class TaskControlAdded( val control: TaskControl ) : Event()
        data class TaskControlRemoved( val control: TaskControl ) : Event()
        data class ParticipantRoleAdded( val role: ParticipantRole ) : Event()
        data class DeviceAssignmentChanged(
            val device: AnyPrimaryDeviceConfiguration,
            val assignedTo: AssignedTo
        ) : Event()
        data class ExpectedParticipantDataAdded( val expectedData: ExpectedParticipantData ) : Event()
        data class ExpectedParticipantDataRemoved( val expectedData: ExpectedParticipantData ) : Event()
    }


    companion object Factory
    {
        @Suppress( "ComplexMethod" )
        fun fromSnapshot( snapshot: StudyProtocolSnapshot ): StudyProtocol
        {
            val protocol = with( snapshot ) { StudyProtocol( ownerId, name, description, id, createdOn ) }
            protocol.applicationData = snapshot.applicationData

            // Add primary devices.
            snapshot.primaryDevices.forEach { protocol.addPrimaryDevice( it ) }

            // Add connected devices.
            val allDevices: List<AnyDeviceConfiguration> =
                snapshot.connectedDevices.plus( snapshot.primaryDevices ).toList()
            snapshot.connections.forEach { c ->
                val primary: AnyPrimaryDeviceConfiguration = allDevices
                    .filterIsInstance<AnyPrimaryDeviceConfiguration>()
                    .firstOrNull { it.roleName == c.connectedToRoleName }
                        ?: throw IllegalArgumentException(
                            "Can't find primary device with role name '${c.connectedToRoleName}' in snapshot."
                        )
                val connected: AnyDeviceConfiguration = allDevices.firstOrNull { it.roleName == c.roleName }
                    ?: throw IllegalArgumentException(
                        "Can't find connected device with role name '${c.roleName}' in snapshot."
                    )
                protocol.addConnectedDevice( connected, primary )
            }

            // Add tasks.
            snapshot.tasks.forEach { protocol.addTask( it ) }

            // Add triggers.
            val triggerIds = snapshot.triggers.keys.sorted()
            if ( triggerIds.isNotEmpty() )
            {
                require( triggerIds.first() == 0 && triggerIds.last() == triggerIds.size - 1 )
                    { "Triggers should be given sequential IDs starting with 0." }
                triggerIds.map {
                    val trigger = checkNotNull( snapshot.triggers[ it ] )
                    protocol.addTrigger( trigger )
                }
            }

            // Add task controls.
            snapshot.taskControls.forEach { control ->
                val triggerMatch = snapshot.triggers.entries.singleOrNull { it.key == control.triggerId }
                    ?: throw IllegalArgumentException(
                        "Can't find trigger with id '${control.triggerId}' in snapshot."
                    )
                val task: TaskConfiguration<*> = protocol.tasks.singleOrNull { it.name == control.taskName }
                    ?: throw IllegalArgumentException(
                        "Can't find task with name '${control.taskName}' in snapshot."
                    )
                val device = protocol.devices.singleOrNull { it.roleName == control.destinationDeviceRoleName }
                    ?: throw IllegalArgumentException(
                        "Can't find device with role name '${control.destinationDeviceRoleName}' in snapshot."
                    )
                protocol.addTaskControl( triggerMatch.value, task, device, control.control )
            }

            // Add expected participant roles and data.
            snapshot.participantRoles.forEach { protocol.addParticipantRole( it ) }
            snapshot.expectedParticipantData.forEach { protocol.addExpectedParticipantData( it ) }

            // Assign devices.
            snapshot.assignedDevices.forEach { (deviceRoleName, assignedParticipantRoles) ->
                val device = requireNotNull( protocol.primaryDevices.singleOrNull { it.roleName == deviceRoleName } )
                    { "Can't find device with role name '$deviceRoleName' in snapshot." }
                protocol.changeDeviceAssignment( device, AssignedTo.Roles( assignedParticipantRoles ) )
            }

            // Events introduced by loading the snapshot are not relevant to a consumer wanting to persist changes.
            protocol.consumeEvents()
            protocol.wasLoadedFromSnapshot( snapshot )

            return protocol
        }
    }


    private var _name: String = name
    /**
     * A unique descriptive name for the protocol assigned by the protocol owner.
     */
    var name: String
        get() = _name
        set( value )
        {
            if ( _name != value )
            {
                _name = value
                event( Event.NameChanged( value ) )
            }
        }

    private var _description: String? = description
    /**
     * An optional description for the study protocol.
     */
    var description: String?
        get() = _description
        set( value )
        {
            if ( _description != value )
            {
                _description = value
                event( Event.DescriptionChanged( value ) )
            }
        }


    /**
     * Add a [primaryDevice] which is responsible for aggregating and synchronizing incoming data.
     * Its role name should be unique in the protocol.
     *
     * @throws IllegalArgumentException when:
     *  - a device with the specified role name already exists
     *  - [primaryDevice] contains invalid default sampling configurations
     * @return True if the [primaryDevice] has been added; false if it is already set as a primary device.
     */
    override fun addPrimaryDevice( primaryDevice: AnyPrimaryDeviceConfiguration ): Boolean
    {
        val isAdded = super.addPrimaryDevice( primaryDevice )
        if ( isAdded )
        {
            _deviceAssignments[ primaryDevice ] = AssignedTo.All
            event( Event.PrimaryDeviceAdded( primaryDevice ) )
        }

        return isAdded
    }

    /**
     * Add a [device] which is connected to a [primaryDevice] within this configuration.
     * Its role name should be unique in the protocol.
     *
     * @throws IllegalArgumentException when:
     *   - a device with the specified role name already exists
     *   - [primaryDevice] is not part of the device configuration
     *   - [device] contains invalid default sampling configurations
     * @return True if the [device] has been added; false if it is already connected to the specified [primaryDevice].
     */
    override fun addConnectedDevice(
        device: AnyDeviceConfiguration,
        primaryDevice: AnyPrimaryDeviceConfiguration
    ): Boolean = super
        .addConnectedDevice( device, primaryDevice )
        .eventIf( true ) { Event.ConnectedDeviceAdded( device, primaryDevice ) }

    /**
     * Set of triggers in the exact sequence by which they were added to the protocol.
     * A `LinkedHashSet` is used to guarantee this order is maintained, allowing to use the index as ID.
     */
    private val _triggers: LinkedHashSet<TriggerConfiguration<*>> = LinkedHashSet()

    /**
     * The list of triggers with assigned IDs which can start or stop tasks in this study protocol.
     */
    val triggers: List<TriggerWithId>
        get() = _triggers.mapIndexed { index, trigger -> TriggerWithId( index, trigger ) }

    /**
     * Stores which tasks need to be started or stopped when the conditions defined by [triggers] are met.
     */
    private val triggerControls: MutableMap<TriggerConfiguration<*>, MutableSet<TaskControl>> = mutableMapOf()

    /**
     * Add a [trigger] to this protocol.
     *
     * @throws IllegalArgumentException when:
     *   - [trigger] does not belong to any device specified in the study protocol
     *   - [trigger] requires a primary device and the specified source device is not a primary device
     * @return The [trigger] and its newly assigned ID, or previously assigned ID in case the trigger is already included in this protocol.
     */
    fun addTrigger( trigger: TriggerConfiguration<*> ): TriggerWithId
    {
        val device = deviceConfiguration.devices.firstOrNull { it.roleName == trigger.sourceDeviceRoleName }
            ?: throw IllegalArgumentException(
                "The passed trigger does not belong to any device specified in this study protocol."
            )

        require( !trigger.requiresPrimaryDevice || device.isPrimary() )
            { "The passed trigger cannot be initiated by the specified device since it is not a primary device." }

        val isAdded = _triggers.add( trigger )
        if ( isAdded )
        {
            triggerControls[ trigger ] = mutableSetOf()
            event( Event.TriggerAdded( trigger ) )
        }

        return TriggerWithId( _triggers.indexOf( trigger ), trigger )
    }

    /**
     * Add a [task] to be started or stopped (determined by [control]) on a [destinationDevice]
     * once a [trigger] within this protocol is initiated.
     * In case the [trigger] or [task] is not yet included in this study protocol, it will be added.
     * The [destinationDevice] needs to be added prior to this call since it needs to be set up as
     * either a primary device or connected device.
     *
     * @throws IllegalArgumentException when [destinationDevice] is not included in this study protocol.
     * @return True if the task control has been added; false if the same control is already present.
     */
    fun addTaskControl(
        trigger: TriggerConfiguration<*>,
        task: TaskConfiguration<*>,
        destinationDevice: AnyDeviceConfiguration,
        control: Control
    ): Boolean
    {
        // The device needs to be included in the study protocol.
        // We cannot add it here since we do not know whether it should be a primary or connected device.
        require( destinationDevice in devices )
            { "The passed device to which the task needs to be sent is not included in this study protocol." }

        // Add trigger and task to ensure they are included in the protocol.
        addTrigger( trigger )
        addTask( task )

        // Add task control.
        val taskControl = TaskControl( trigger, task, destinationDevice, control )
        return triggerControls[ trigger ]!!
            .add( taskControl )
            .eventIf( true ) { Event.TaskControlAdded( taskControl ) }
    }

    /**
     * Add a task to be started or stopped on a device once a trigger within this protocol is initiated.
     * In case the trigger or task defined in [control] is not yet included in this study protocol, it will be added.
     * The destination device defined in [control] needs to be added prior to this call since it needs to be set up as
     * either a primary device or connected device.
     *
     * @throws IllegalArgumentException when the destination device is not included in this study protocol.
     * @return True if the task control has been added; false if the same control is already present.
     */
    fun addTaskControl( control: TaskControl ): Boolean =
        addTaskControl( control.trigger, control.task, control.destinationDevice, control.control )

    /**
     * Gets all conditions which control that tasks get started or stopped on devices in this protocol by the specified [trigger].
     *
     * @throws IllegalArgumentException when [trigger] is not part of this study protocol.
     */
    fun getTaskControls( trigger: TriggerConfiguration<*> ): Iterable<TaskControl>
    {
        require( trigger in _triggers ) { "The passed trigger is not part of this study protocol." }

        return triggerControls[ trigger ]!!
    }

    /**
     * Gets all conditions which control that tasks get started or stopped on devices in this protocol
     * by the trigger with [triggerId].
     *
     * @throws IllegalArgumentException when a trigger with [triggerId] is not defined in this study protocol.
     */
    fun getTaskControls( triggerId: Int ): Iterable<TaskControl>
    {
        val trigger = triggers.firstOrNull { it.id == triggerId }?.trigger
        requireNotNull( trigger ) { "There is no trigger with ID \"$triggerId\" in this study protocol." }

        return triggerControls[ trigger ]!!
    }

    /**
     * Gets all the tasks triggered for the specified [device].
     */
    fun getTasksForDevice( device: AnyDeviceConfiguration ): Set<TaskConfiguration<*>>
    {
        return triggerControls
            .flatMap { it.value }
            .filter { it.destinationDevice == device }
            .map { it.task }
            .toSet()
    }

    /**
     * Add a [task] to this configuration.
     *
     * @throws IllegalArgumentException in case a task with the specified name already exists.
     * @return True if the [task] has been added; false if it is already included in this configuration.
     */
    override fun addTask( task: TaskConfiguration<*> ): Boolean =
        super.addTask( task )
        .eventIf( true ) { Event.TaskAdded( task ) }

    /**
     * Remove a [task] currently present in this configuration
     * including removing it from any [TaskControl]'s which initiate it.
     *
     * @return True if the [task] has been removed; false if it is not included in this configuration.
     */
    override fun removeTask( task: TaskConfiguration<*> ): Boolean
    {
        // Remove all controls which control this task.
        triggerControls.values.forEach { controls ->
            val taskControls = controls.filter { it.task == task }
            controls.removeAll( taskControls )
            taskControls.forEach { event( Event.TaskControlRemoved( it ) ) }
        }

        // Remove task itself.
        return taskConfiguration
            .removeTask( task )
            .eventIf( true ) { Event.TaskRemoved( task ) }
    }

    /**
     * Add a participant role which can be assigned to participants in the study.
     *
     * @throws IllegalArgumentException in case a differing [role] with a matching role name is already added.
     * @return True if the [role] has been added; false in case the same [role] has already been added before.
     */
    override fun addParticipantRole( role: ParticipantRole ): Boolean =
        super.addParticipantRole( role )
        .eventIf( true ) { Event.ParticipantRoleAdded( role ) }

    /**
     * Add expected participant data to be input by users.
     *
     * @throws IllegalArgumentException if:
     *  - [expectedData] is assigned to a participant role which is not part of this [ProtocolParticipantConfiguration]
     *  - a differing [ParticipantAttribute] with a matching input data type is already added
     *  - [expectedParticipantData] already contains an input data type which can be input by the same role
     * @return True if the [expectedData] has been added; false in case the same [expectedData] has already been added before.
     */
    override fun addExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean =
        super.addExpectedParticipantData( expectedData )
        .eventIf( true ) { Event.ExpectedParticipantDataAdded( expectedData ) }

    /**
     * Remove expected participant data to be input by users.
     *
     * @return True if the [expectedData] has been removed; false if it is not included in this configuration.
     */
    override fun removeExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean =
        super.removeExpectedParticipantData( expectedData )
        .eventIf( true ) { Event.ExpectedParticipantDataRemoved( expectedData ) }

    /**
     * Replace the expected participant data to be input by users with the specified [expectedData].
     *
     * TODO: This is currently defined in `StudyProtocol` rather than `ParticipantDataConfiguration` due to the need to track events.
     *   Once eventing is implemented on `ParticipantDataConfiguration`, this can be moved where it logically belongs.
     *
     * @throws IllegalArgumentException if:
     *  - [expectedData] is assigned to a participant role which is not part of this protocol
     *  - [expectedData] contains differing [ParticipantAttribute]s with the same input data type
     *  - [expectedData] contains multiple attributes of the same input data type which are assigned to the same role
     * @return True if any expected data has been replaced; false if the specified [expectedData] was the same as those already set.
     */
    fun replaceExpectedParticipantData( expectedData: Set<ExpectedParticipantData> ): Boolean
    {
        // Throw when expected data is invalid so that the set isn't added partially.
        expectedData.hasNoConflicts( exceptionOnConflict = true )
        require( expectedData.all { isValidAssignment( it.assignedTo ) } ) // TODO: Make this part of `hasNoConflicts`?
            { "Expected data contains participant role names which aren't part of the participant configuration." }

        val toRemove = expectedParticipantData.minus( expectedData )
        val toAdd = expectedData.minus( expectedParticipantData )

        if ( toRemove.isEmpty() && toAdd.isEmpty() ) return false

        toRemove.forEach { removeExpectedParticipantData( it ) }
        toAdd.forEach { addExpectedParticipantData( it ) }
        return true
    }

    /**
     * For each of the primary device configurations in this protocol, the participant roles it has been [AssignedTo].
     * By default, primary devices are [AssignedTo.All] roles.
     */
    val deviceAssignments: Map<AnyPrimaryDeviceConfiguration, AssignedTo>
        get() = _deviceAssignments.toMap()

    private val _deviceAssignments: MutableMap<AnyPrimaryDeviceConfiguration, AssignedTo> = mutableMapOf()

    /**
     * Change who the primary [device] is [assignedTo].
     * By default, primary devices are [AssignedTo.All] roles.
     *
     * @throws IllegalArgumentException if:
     *  - [device] is not part of this protocol
     *  - [assignedTo] contains participant roles which are not part of this protocol
     */
    fun changeDeviceAssignment( device: AnyPrimaryDeviceConfiguration, assignedTo: AssignedTo ): Boolean
    {
        require( _deviceAssignments.containsKey( device ) )
            { "The device configuration is not part of this protocol." }
        require( isValidAssignment( assignedTo ) )
            { "One of the assigned participant roles is not part of this protocol." }

        val isChanged = _deviceAssignments.put( device, assignedTo ) != assignedTo
        if ( isChanged ) event( Event.DeviceAssignmentChanged( device, assignedTo ) )
        return isChanged
    }

    /**
     * Application-specific data to be stored as part of the study protocol
     * which will be included in all deployments of this study protocol.
     *
     * This can be used by infrastructures or concrete applications which require exchanging additional data
     * between the protocols and clients subsystems, outside of scope or not yet supported by CARP core.
     */
    var applicationData: ApplicationData? = null


    /**
     * All possible issues related to incomplete or problematic configuration of a [StudyProtocol] which might prevent deployment.
     */
    private val possibleDeploymentIssues: List<DeploymentIssue> = listOf(
        NoPrimaryDeviceError(),
        OnlyOptionalDevicesWarning(),
        UnstartedTasksWarning(),
        BackgroundTaskWithNoMeasuresWarning(),
        UseCompositeTaskWarning(),
        UnusedDevicesWarning(),
        UnexpectedMeasuresWarning()
    )

    /**
     * Returns warnings and errors for the current configuration of the study protocol.
     */
    fun getDeploymentIssues(): Iterable<DeploymentIssue> =
        possibleDeploymentIssues.filter { it.isIssuePresent( this ) }

    /**
     * Based on the current configuration, determines whether the study protocol can be deployed.
     * The protocol can be deployed as long as no [DeploymentError]s are identified in [getDeploymentIssues].
     *
     * In order to retrieve specific deployment issues, call [getDeploymentIssues].
     */
    fun isDeployable(): Boolean =
        !getDeploymentIssues().any { it is DeploymentError }


    /**
     * Get an immutable snapshot of the current state of this [StudyProtocol] using the specified snapshot [version].
     */
    override fun getSnapshot( version: Int ): StudyProtocolSnapshot =
        StudyProtocolSnapshot.fromProtocol( this, version )
}
