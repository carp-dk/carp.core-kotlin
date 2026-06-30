package dk.cachet.carp.protocols.infrastructure.test

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.*
import dk.cachet.carp.protocols.domain.StudyProtocol


/**
 * Creates a study protocol using the default initialization (no devices, tasks, or triggers),
 * and initializes the infrastructure serializer to be aware about polymorph stub testing classes.
 */
fun createEmptyProtocol( name: String = "Test protocol" ): StudyProtocol
{
    JSON = createDefaultJSON( STUBS_SERIAL_MODULE )

    val alwaysSameOwnerId = UUID( "27879e75-ccc1-4866-9ab3-4ece1b735052" )
    return StudyProtocol( alwaysSameOwnerId, name, "Test description" )
}

/**
 * Creates a study protocol with a single primary device.
 */
fun createSinglePrimaryDeviceProtocol( primaryDeviceName: String = "Primary" ): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val primary = StubPrimaryDeviceConfiguration( primaryDeviceName )
    protocol.addPrimaryDevice( primary )
    return protocol
}

/**
 * Creates a study protocol with a single primary device which has a single connected device.
 */
fun createSinglePrimaryWithConnectedDeviceProtocol(
    primaryDeviceName: String = "Primary",
    connectedDeviceName: String = "Connected"
): SinglePrimaryWithConnectedTestProtocol
{
    val protocol = createEmptyProtocol()
    val primary = StubPrimaryDeviceConfiguration( primaryDeviceName )
    protocol.addPrimaryDevice( primary )
    val connected = StubDeviceConfiguration( connectedDeviceName )
    protocol.addConnectedDevice( connected, primary )

    return SinglePrimaryWithConnectedTestProtocol( protocol, primary, connected )
}

data class SinglePrimaryWithConnectedTestProtocol(
    val protocol: StudyProtocol,
    val primary: AnyPrimaryDeviceConfiguration,
    val connected: AnyDeviceConfiguration
)

/**
 * Creates a study protocol with two primary devices, each assigned to a different participant role.
 */
fun createTwoDevicesAndRolesProtocol(): TwoDevicesAndRolesTestProtocol
{
    val protocol = createEmptyProtocol()
    val device1 = StubPrimaryDeviceConfiguration( "Device 1" )
    val device2 = StubPrimaryDeviceConfiguration( "Device 2" )
    val role1 = ParticipantRole( "Role 1", isOptional = false )
    val role2 = ParticipantRole( "Role 2", isOptional = false )
    val role1Assignment = AssignedTo.Roles( setOf( role1.role ) )
    val role2Assignment = AssignedTo.Roles( setOf( role2.role ) )

    with ( protocol ) {
        addPrimaryDevice( device1 )
        addPrimaryDevice( device2 )
        addParticipantRole( role1 )
        addParticipantRole( role2 )
        changeDeviceAssignment( device1, role1Assignment )
        changeDeviceAssignment( device2, role2Assignment )
    }

    return TwoDevicesAndRolesTestProtocol( protocol, device1, device2, role1.role, role2.role )
}

data class TwoDevicesAndRolesTestProtocol(
    val protocol: StudyProtocol,
    val device1: AnyPrimaryDeviceConfiguration,
    val device2: AnyPrimaryDeviceConfiguration,
    val role1Name: String,
    val role2Name: String
)

/**
 * Creates a study protocol with a couple of devices and tasks added.
 */
fun createComplexProtocol(): StudyProtocol
{
    val protocol = createEmptyProtocol()
    val primaryDevice = StubPrimaryDeviceConfiguration()
    val connectedDevice = StubDeviceConfiguration()
    val chainedPrimaryDevice = StubPrimaryDeviceConfiguration( "Chained primary" )
    val chainedConnectedDevice = StubDeviceConfiguration( "Chained connected" )
    val trigger = StubTriggerConfiguration( connectedDevice )
    val measures = listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) )
    val task = StubTaskConfiguration( "Task", measures )
    val mainRole = ParticipantRole( "Role", false )
    val optionalRole = ParticipantRole( "Optional role", true )
    val commonExpectedData =
        ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) ),
            AssignedTo.All
        )
    val mainRoleData =
        ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            AssignedTo.Roles( setOf( mainRole.role ) )
        )
    with ( protocol )
    {
        addPrimaryDevice( primaryDevice )
        addConnectedDevice( connectedDevice, primaryDevice )
        addConnectedDevice( chainedPrimaryDevice, primaryDevice )
        addConnectedDevice( chainedConnectedDevice, chainedPrimaryDevice )
        addTaskControl( trigger, task, primaryDevice, TaskControl.Control.Start )
        addParticipantRole( mainRole )
        addParticipantRole( optionalRole )
        changeDeviceAssignment( primaryDevice, AssignedTo.Roles( setOf( mainRole.role ) ) )
        addExpectedParticipantData( commonExpectedData )
        addExpectedParticipantData( mainRoleData )
    }

    return protocol
}
