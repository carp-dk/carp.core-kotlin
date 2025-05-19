package dk.cachet.carp.analytics.domain.trigger

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TriggerActivation(
    val id: UUID,
    val triggerId: UUID,
    val studyId: UUID,
    val firedAt: Instant,
    val workflowExecutionId: UUID?
)
