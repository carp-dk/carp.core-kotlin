package dk.cachet.carp.deployments.application.users

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * An [invitation] to participate in an active study deployment using the [assignedDevices].
 * Some devices which the participant is invited to might already be registered.
 * If the participant wants to use a different device, they will need to unregister the existing device first.
 */
@Serializable
@JsExport
data class ActiveParticipationInvitation(
    val participation: Participation,
    val invitation: StudyInvitation,
    val assignedDevices: Set<AssignedPrimaryDevice>
)
