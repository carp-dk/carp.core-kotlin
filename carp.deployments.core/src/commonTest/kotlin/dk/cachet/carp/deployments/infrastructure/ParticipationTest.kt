package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.Participation
import kotlin.test.*


/**
 * Tests for [Participation] relying on core infrastructure.
 */
class ParticipationTest
{
    @Test
    fun can_serialize_and_deserialize_participation_using_JSON()
    {
        val participation = Participation( UUID.randomUUID() )

        val serialized = JSON.encodeToString( participation )
        val parsed: Participation = JSON.decodeFromString( serialized )

        assertEquals( participation, parsed )
    }
}
