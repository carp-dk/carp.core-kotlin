package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.studyDeploymentFor
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for [StudyDeploymentStatus] relying on core infrastructure.
 */
class StudyDeploymentStatusTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }

    @Test
    fun can_serialize_and_deserialize_deployment_status_using_JSON()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        deployment.registrableDevices.forEach {
            deployment.registerDevice( it.device, it.device.createRegistration() )
        }
        val primaryDeviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, primaryDeviceDeployment.lastUpdatedOn )
        val status: StudyDeploymentStatus = deployment.getStatus()

        val serialized: String = JSON.encodeToString( status )
        val parsed: StudyDeploymentStatus = JSON.decodeFromString( serialized )

        assertEquals( status, parsed )
    }

    @Test
    fun serializing_deployment_when_unknown_devices_are_involved()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Unknown" )
        protocol.addPrimaryDevice( primary )
        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = JSON.encodeToString( snapshot )

        // Mimic an unknown device type.
        serialized = serialized.makeUnknown( primary, "com.unknown.UnknownPrimaryDevice" )

        // Create deployment based on protocol with custom types and serialize its status.
        val snapshotWithCustom: StudyProtocolSnapshot = JSON.decodeFromString( serialized )
        val deployment = StudyDeployment.fromInvitations(
            snapshotWithCustom,
            listOf(
                ParticipantInvitation(
                    UUID.randomUUID(),
                    AssignedTo.All,
                    UsernameAccountIdentity( "Test" ),
                    StudyInvitation( "Test" )
                )
            )
        )
        val status = JSON.encodeToString( deployment.getStatus() )

        // This verifies whether the 'CustomPrimaryDeviceConfiguration' wrapper is removed in JSON output.
        assertTrue { status.contains( "\"device\":{\"$CLASS_DISCRIMINATOR\":\"com.unknown.UnknownPrimaryDevice\"" ) }
    }
}
