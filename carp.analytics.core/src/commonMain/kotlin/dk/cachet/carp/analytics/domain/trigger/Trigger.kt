package dk.cachet.carp.analytics.domain.trigger

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed interface Trigger {
    val id: UUID
    val studyId: UUID
    val workflowId: UUID
    val name: String
    val createdAt: Instant


    fun activate(at: Instant, executionId: UUID): TriggerActivation

}
