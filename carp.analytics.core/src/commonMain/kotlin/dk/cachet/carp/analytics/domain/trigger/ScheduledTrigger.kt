package dk.cachet.carp.analytics.domain.trigger

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ScheduledTrigger")
data class ScheduledTrigger(
    override val id: UUID,
    override val studyId: UUID,
    override val workflowId: UUID,
    override val name: String,
    val cron: CronExpression,
    override val createdAt: Instant,
    val updatedAt: Instant? = null,
    val active: Boolean = true,
    val lastFiredAt: Instant? = null


) : Trigger {
    override fun activate(at: Instant, executionId: UUID): TriggerActivation =
        TriggerActivation(UUID.randomUUID(), id, studyId, at, executionId)

    fun getNextScheduledTime(from: Instant): kotlinx.datetime.LocalDateTime? =
        cron.getNextScheduledTime(from)
}
