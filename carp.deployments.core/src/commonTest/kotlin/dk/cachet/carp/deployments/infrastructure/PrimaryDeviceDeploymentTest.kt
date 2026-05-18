package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import kotlin.test.*


/**
 * Tests for [PrimaryDeviceDeployment] relying on core infrastructure.
 */
class PrimaryDeviceDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }

    @Test
    fun can_serialize_and_deserialize_devicedeployment_using_JSON()
    {
        val device = StubPrimaryDeviceConfiguration()
        val connected = StubDeviceConfiguration( "Connected" )
        val task = StubTaskConfiguration( "Task" )
        val trigger = StubTriggerConfiguration( connected.roleName )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "test" ) )
        )

        val deployment = PrimaryDeviceDeployment(
            device,
            device.createRegistration(),
            setOf( connected ),
            mapOf( connected.roleName to connected.createRegistration() ),
            setOf( task ),
            mapOf( 0 to trigger ),
            setOf( TaskControl( 0, task.name, connected.roleName, TaskControl.Control.Start ) ),
            setOf( expectedData ),
            ApplicationData( "some data" )
        )

        val json = JSON.encodeToString( deployment )
        val parsed: PrimaryDeviceDeployment = JSON.decodeFromString( json )
        assertEquals( deployment, parsed )
    }
}
