package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Assign participant roles specified in a study protocol ([assignedRoles])
 * to the participant identified by [participantId].
 */
@Serializable
@JsExport
data class AssignedParticipantRoles( val participantId: UUID, val assignedRoles: AssignedTo )

/**
 * Get the unique set of participant IDs defined in a collection of [AssignedParticipantRoles].
 */
@JsExport
fun Collection<AssignedParticipantRoles>.participantIds(): Set<UUID> = this.map { it.participantId }.toSet()

/**
 * Get the unique set of participant roles defined in a collection of [AssignedParticipantRoles].
 */
@JsExport
fun Collection<AssignedParticipantRoles>.participantRoles(): Set<String> = this
    .flatMap {
        when ( it.assignedRoles )
        {
            is AssignedTo.All -> emptySet()
            is AssignedTo.Roles -> it.assignedRoles.roleNames
        }
    }
    .toSet()
