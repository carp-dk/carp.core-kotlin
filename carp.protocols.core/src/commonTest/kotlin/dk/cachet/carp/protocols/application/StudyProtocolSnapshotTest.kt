package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests for [StudyProtocolSnapshot].
 */
class StudyProtocolSnapshotTest
{
    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun equals_and_hashcode_when_comparing_snapshots_of_same_protocol()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val snapshot1: StudyProtocolSnapshot = protocol.getSnapshot()
        val snapshot2: StudyProtocolSnapshot = protocol.getSnapshot()

        assertTrue( snapshot1 == snapshot2 )
        assertTrue( snapshot1.hashCode() == snapshot2.hashCode() )
    }

    @Test
    fun equals_and_hashcode_when_comparing_snapshots_of_modified_protocol()
    {
        val protocol: StudyProtocol = createComplexProtocol()
        val original: StudyProtocolSnapshot = protocol.getSnapshot()

        // New primary device.
        val primaryDevice = StubPrimaryDeviceConfiguration( "New primary" )
        protocol.addPrimaryDevice( primaryDevice )
        val newPrimaryDeviceSnapshot = protocol.getSnapshot()
        assertTrue( original != newPrimaryDeviceSnapshot )
        assertTrue( original.hashCode() != newPrimaryDeviceSnapshot.hashCode() )

        // New connected device.
        val connectedDevice = StubDeviceConfiguration( "New connected" )
        protocol.addConnectedDevice( connectedDevice, primaryDevice )
        val newConnectedDeviceSnapshot = protocol.getSnapshot()
        assertTrue( newPrimaryDeviceSnapshot != newConnectedDeviceSnapshot )
        assertTrue( newPrimaryDeviceSnapshot.hashCode() != newConnectedDeviceSnapshot.hashCode() )

        // New trigger.
        val trigger = StubTriggerConfiguration( primaryDevice )
        protocol.addTrigger( trigger )
        val newTriggerSnapshot = protocol.getSnapshot()
        assertTrue( newConnectedDeviceSnapshot != newTriggerSnapshot )
        assertTrue( newConnectedDeviceSnapshot.hashCode() != newTriggerSnapshot.hashCode() )

        // New task.
        val task = StubTaskConfiguration( "New task" )
        protocol.addTask( task )
        val newTaskSnapshot = protocol.getSnapshot()
        assertTrue( newTriggerSnapshot != newTaskSnapshot )
        assertTrue( newTriggerSnapshot.hashCode() != newTaskSnapshot.hashCode() )

        // New triggered task.
        protocol.addTaskControl( trigger, task, primaryDevice, TaskControl.Control.Start )
        val newTriggeredTaskSnapshot = protocol.getSnapshot()
        assertTrue( newTaskSnapshot != newTriggeredTaskSnapshot )
        assertTrue( newTaskSnapshot.hashCode() != newTriggeredTaskSnapshot.hashCode() )
    }

    @Test
    fun order_of_tasks_devices_and_expected_participant_data_in_snapshot_does_not_matter_for_equality_or_hashcode()
    {
        val primaryDevices = listOf<AnyPrimaryDeviceConfiguration>(
            StubPrimaryDeviceConfiguration( "M1" ),
            StubPrimaryDeviceConfiguration( "M2" )
        )
        val connectedDevices = listOf<AnyDeviceConfiguration>(
            StubDeviceConfiguration( "C1" ),
            StubDeviceConfiguration( "C2" )
        )
        val connections = listOf(
            StudyProtocolSnapshot.DeviceConnection( "C1", "M1" ),
            StudyProtocolSnapshot.DeviceConnection( "C2", "M2" )
        )
        val tasks = listOf<TaskConfiguration<*>>( StubTaskConfiguration( "T1" ), StubTaskConfiguration( "T2" ) )
        val triggers = mapOf<Int, TriggerConfiguration<*>>(
            0 to StubTriggerConfiguration( primaryDevices[ 0 ] ),
            1 to StubTriggerConfiguration( primaryDevices[ 1 ] )
        )
        val triggeredTasks = listOf(
            TaskControl( 0, "T1", "C1", TaskControl.Control.Start ),
            TaskControl( 1, "T2", "C2", TaskControl.Control.Start )
        )
        val participantRoles = listOf(
            ParticipantRole( "Role 1", true ),
            ParticipantRole( "Role 2", false )
        )
        val expectedParticipantData = listOf(
            ExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) ) ),
            ExpectedParticipantData( ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "othertype" ) ) )
        )
        val assignedDevices = mapOf(
            "M1" to setOf( "Role 1" ),
            "M2" to setOf( "Role 2" )
        )

        val protocolId = UUID.randomUUID()
        val createdOn = Clock.System.now()
        val ownerId = UUID( "ef26be3f-2de8-4779-a608-bb6e027e4b75" )
        val name = "Study name"
        val description = "Description"
        val snapshot = StudyProtocolSnapshot(
            protocolId,
            createdOn,
            0,
            ownerId,
            name,
            description,
            primaryDevices.toSet(), connectedDevices.toSet(), connections.toSet(),
            tasks.toSet(), triggers, triggeredTasks.toSet(),
            participantRoles.toSet(), assignedDevices, expectedParticipantData.toSet(), ApplicationData( "" )
        )
        val reorganizedSnapshot = StudyProtocolSnapshot(
            protocolId,
            createdOn,
            0,
            ownerId,
            name,
            description,
            primaryDevices.reversed().toSet(), connectedDevices.reversed().toSet(), connections.reversed().toSet(),
            tasks.reversed().toSet(), triggers, triggeredTasks.reversed().toSet(),
            participantRoles.reversed().toSet(), assignedDevices, expectedParticipantData.reversed().toSet(), ApplicationData( "" )
        )

        assertEquals( snapshot, reorganizedSnapshot )
        assertEquals( snapshot.hashCode(), reorganizedSnapshot.hashCode() )
    }

    @Test
    fun order_of_triggers_matters_for_snapshot_equality()
    {
        val device1 = StubPrimaryDeviceConfiguration( "One" )
        val device2 = StubPrimaryDeviceConfiguration( "Two" )
        val trigger1 = StubTriggerConfiguration( "One" )
        val trigger2 = StubTriggerConfiguration( "Two" )

        // Create two identical base protocols. protocol1 is cloned to make sure `createdOn` is the same.
        val protocol1 = createEmptyProtocol()
        val protocol2 = StudyProtocol.fromSnapshot( protocol1.getSnapshot() )

        val snapshot1: StudyProtocolSnapshot = protocol1.apply {
            addPrimaryDevice( device1 )
            addPrimaryDevice( device2 )
            addTrigger( trigger1 )
            addTrigger( trigger2 )
        }.getSnapshot()

        val snapshot2: StudyProtocolSnapshot = protocol2.apply {
            addPrimaryDevice( device1 )
            addPrimaryDevice( device2 )
            addTrigger( trigger2 )
            addTrigger( trigger1 )
        }.getSnapshot()

        // Since the triggers were added in a different order, they were assigned different IDs.
        assertNotEquals( snapshot1, snapshot2 )
    }

    @Test
    fun trigger_ids_need_to_sequential_starting_from_zero()
    {
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val task = StubTaskConfiguration()
        val trigger = StubTriggerConfiguration( primaryDevice )

        val correctSnapshot = StudyProtocolSnapshot(
            UUID.randomUUID(),
            Clock.System.now(),
            0,
            UUID.randomUUID(),
            "Name",
            "Description",
            primaryDevices = setOf( primaryDevice ),
            tasks = setOf( task ),
            triggers = mapOf( 0 to trigger ),
            taskControls = setOf( TaskControl( 0, task.name, primaryDevice.roleName, TaskControl.Control.Start ) )
        )
        StudyProtocol.fromSnapshot( correctSnapshot )

        val wrongSnapshot = correctSnapshot.copy(
            triggers = mapOf( 1 to trigger ),
            taskControls = setOf( TaskControl( 1, task.name, primaryDevice.roleName, TaskControl.Control.Start) )
        )
        assertFailsWith<IllegalArgumentException> { StudyProtocol.fromSnapshot( wrongSnapshot ) }
    }
}
