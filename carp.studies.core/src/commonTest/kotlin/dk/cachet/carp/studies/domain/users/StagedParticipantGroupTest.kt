package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import kotlin.test.*


/**
 * Tests for [StagedParticipantGroup].
 */
class StagedParticipantGroupTest
{
    @Test
    fun addParticipants_succeeds()
    {
        val group = StagedParticipantGroup()
        val roleAssignment = AssignedParticipantRoles( participantId = UUID.randomUUID(), AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )

        assertEquals( roleAssignment.participantId, group.participantIds.singleOrNull() )
        assertEquals( roleAssignment, group.roleAssignments.singleOrNull() )
    }

    @Test
    fun addParticipants_fails_when_already_deployed()
    {
        val group = StagedParticipantGroup()
        val roleAssignment = AssignedParticipantRoles( participantId = UUID.randomUUID(), AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )
        group.markAsDeployed()

        val newRoleAssignment = AssignedParticipantRoles( participantId = UUID.randomUUID(), AssignedTo.All )
        assertFailsWith<IllegalStateException> { group.addParticipants( setOf( newRoleAssignment ) ) }
    }

    @Test
    fun markAsDeployed_succeeds()
    {
        val group = StagedParticipantGroup()
        val roleAssignment = AssignedParticipantRoles( participantId = UUID.randomUUID(), AssignedTo.All )
        group.addParticipants( setOf( roleAssignment ) )
        assertFalse( group.isDeployed )

        group.markAsDeployed()

        assertTrue( group.isDeployed )
        assertEquals( roleAssignment, group.roleAssignments.singleOrNull() )
    }

    @Test
    fun markAsDeployed_fails_when_no_participants_are_added()
    {
        val group = StagedParticipantGroup()
        assertFailsWith<IllegalStateException> { group.markAsDeployed() }
    }
}
