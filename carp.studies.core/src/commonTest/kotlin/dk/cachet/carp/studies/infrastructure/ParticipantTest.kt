package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.Participant
import kotlin.test.*


/**
 * Tests for [Participant] relying on core infrastructure.
 */
class ParticipantTest
{
    @Test
    fun can_serialize_and_deserialize_participant_using_JSON()
    {
        val participant = Participant( AccountIdentity.fromEmailAddress( "test@test.com" ) )

        val serialized: String = JSON.encodeToString( participant )
        val parsed: Participant = JSON.decodeFromString( serialized )

        assertEquals( participant, parsed )
    }
}
