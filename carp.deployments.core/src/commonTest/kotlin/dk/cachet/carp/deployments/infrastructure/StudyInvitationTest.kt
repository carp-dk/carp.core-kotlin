package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.StudyInvitation
import kotlin.test.*


/**
 * Tests for [StudyInvitation] relying on core infrastructure.
 */
class StudyInvitationTest
{
    @Test
    fun can_serialize_and_deserialize_study_invitation_using_JSON()
    {
        val applicationData = ApplicationData( """{"extraData":"42"}""" )
        val invitation = StudyInvitation( "Test", "Description", applicationData )

        val serialized = JSON.encodeToString( invitation )
        val parsed: StudyInvitation = JSON.decodeFromString( serialized )

        assertEquals( invitation, parsed )
    }

    @Test
    fun can_deserialize_study_invitation_without_applicationData()
    {
        val serialized = """{"name":"Test","description":"Description"}"""

        JSON.decodeFromString<StudyInvitation>( serialized )
    }
}
