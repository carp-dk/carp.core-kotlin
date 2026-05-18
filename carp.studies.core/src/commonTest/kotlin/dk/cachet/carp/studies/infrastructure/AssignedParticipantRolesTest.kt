package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import kotlin.test.*


/**
 * Tests for [AssignedParticipantRoles] which rely on core infrastructure.
 */
class AssignedParticipantRolesTest
{
    @Test
    fun can_serialize_and_deserialize_assigned_participant_using_JSON()
    {
        val assign = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        val serialized: String = JSON.encodeToString( assign )
        val parsed: AssignedParticipantRoles = JSON.decodeFromString( serialized )

        assertEquals( assign, parsed )
    }
}
