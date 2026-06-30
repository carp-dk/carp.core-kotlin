package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for [ActiveParticipationInvitation] relying on core infrastructure.
 */
class ActiveParticipationInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_active_participation_invitation_using_JSON()
    {
        val json = createTestJSON()
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation( "Some study" ),
            setOf( AssignedPrimaryDevice( primaryDevice ) )
        )

        val serialized = json.encodeToString( invitation )
        val parsed = json.decodeFromString<ActiveParticipationInvitation>( serialized )

        assertEquals( invitation, parsed )
    }

    @Test
    fun serializing_unknown_primary_device_removes_the_wrapper()
    {
        val json = createTestJSON()
        val unknownPrimaryDevice = StubPrimaryDeviceConfiguration().makeUnknown( json, "unknown.device" )
        val invitation = ActiveParticipationInvitation(
            Participation( UUID.randomUUID() ),
            StudyInvitation( "Some study" ),
            setOf( AssignedPrimaryDevice( unknownPrimaryDevice ) )
        )

        val serialized = json.encodeToString( invitation )
        assertTrue( !serialized.contains( "CustomPrimaryDeviceConfiguration" ) )
    }
}
