package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.participantIds
import kotlinx.serialization.*


/**
 * A group of participants configured during recruitment,
 * intended to be deployed as a whole once configuration is completed.
 */
@Serializable
data class StagedParticipantGroup(
    /**
     * The identifier for this participant group, used as deployment ID once the participant group is deployed.
     */
    val id: UUID = UUID.randomUUID(),
    /**
     * An optional name to represent the group of participants.
     */
    var name: String? = null,
)
{
    private val _roleAssignments: MutableSet<AssignedParticipantRoles> = mutableSetOf()
    /**
     * The roles assigned to participants in this group.
     */
    val roleAssignments: Set<AssignedParticipantRoles>
        get() = _roleAssignments
    val participantIds: Set<UUID>
        get() = _roleAssignments.participantIds()

    /**
     * Determines whether this participant group has been deployed.
     */
    var isDeployed: Boolean = false
        private set

    /**
     * Add [participants] with assigned roles to this group.
     * This is only allowed when the group hasn't been deployed yet.
     *
     * @throws IllegalStateException when this participant group is already deployed.
     */
    fun addParticipants( participants: Set<AssignedParticipantRoles> )
    {
        check( !isDeployed ) { "Can't add participants after a participant group has been deployed." }

        _roleAssignments.addAll( participants )
    }

    /**
     * Replace all participants in this group.
     * This is only allowed when the group hasn't been deployed yet.
     *
     * @throws IllegalStateException when this participant group is already deployed.
     */
    fun replaceParticipants( participants: Set<AssignedParticipantRoles> )
    {
        check( !isDeployed ) { "Can't update participants after a participant group has been deployed." }

        _roleAssignments.clear()
        _roleAssignments.addAll( participants )
    }

    /**
     * Specify that a deployment for this participant group has been created.
     *
     * @throws IllegalStateException when no participants to deploy are specified.
     */
    fun markAsDeployed()
    {
        check( participantIds.isNotEmpty() ) { "No participants specified to deploy." }

        isDeployed = true
    }
}
