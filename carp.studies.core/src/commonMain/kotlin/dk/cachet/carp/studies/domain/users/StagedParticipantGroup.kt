package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
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
    val name: String? = null,
)
{
    private val _participantIds: MutableSet<UUID> = mutableSetOf()
    val participantIds: Set<UUID>
        get() = _participantIds

    /**
     * Determines whether this participant group has been deployed.
     */
    var isDeployed: Boolean = false
        private set



    /**
     * Add participants with [participantIds] to this group.
     * This is only allowed when the group hasn't been deployed yet.
     *
     * @throws IllegalStateException when this participant group is already deployed.
     */
    fun addParticipants( participantIds: Set<UUID> )
    {
        check( !isDeployed ) { "Can't add participant after a participant group has been deployed." }

        _participantIds.addAll( participantIds )
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
