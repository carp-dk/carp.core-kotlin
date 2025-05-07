package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployments.domain.studyDeploymentFor
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for [StudyDeployment] relying on core infrastructure.
 */
class StudyDeploymentTest
{
    @BeforeTest
    fun initializeSerializer()
    {
        JSON = createTestJSON()
    }


    @Test
    fun cant_initialize_deployment_with_invalid_snapshot()
    {
        // Initialize valid protocol.
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        val connected = StubPrimaryDeviceConfiguration( "Connected" )
        protocol.addPrimaryDevice( primary )
        protocol.addConnectedDevice( connected, primary )
        val snapshot = protocol.getSnapshot()

        // Create invalid snapshot by editing JSON.
        val json = JSON.encodeToString( snapshot )
        val invalidJson = json.replaceFirst( "\"Primary\"", "\"Non-existing device\"" )
        val invalidSnapshot: StudyProtocolSnapshot = JSON.decodeFromString( invalidJson )

        val invitations = listOf(
            ParticipantInvitation(
                UUID.randomUUID(),
                AssignedTo.All,
                UsernameAccountIdentity( "Test" ),
                StudyInvitation( "Test" )
            )
        )
        assertFailsWith<IllegalArgumentException>
        {
            StudyDeployment.fromInvitations( invalidSnapshot, invitations )
        }
    }

    @Test
    fun create_deployment_fromSnapshot_with_custom_extending_types_succeeds()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Unknown" )
        protocol.addPrimaryDevice( primary )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( primary, registration )

        // Mimic unknown device and registration.
        var serialized: String = JSON.encodeToString( deployment.getSnapshot() )
        serialized = serialized.makeUnknown( primary, "Unknown" )
        serialized = serialized.makeUnknown( registration )

        val snapshot: StudyDeploymentSnapshot = JSON.decodeFromString( serialized )
        StudyDeployment.fromSnapshot( snapshot )
    }
}
