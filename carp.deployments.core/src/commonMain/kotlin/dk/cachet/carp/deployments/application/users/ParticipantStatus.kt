package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Provides information on the status of a participant in a study deployment.
 */
@Serializable
@JsExport
data class ParticipantStatus(
    val participantId: UUID,
    val assignedParticipantRoles: AssignedTo,
    val assignedPrimaryDeviceRoleNames: Set<String>
)
