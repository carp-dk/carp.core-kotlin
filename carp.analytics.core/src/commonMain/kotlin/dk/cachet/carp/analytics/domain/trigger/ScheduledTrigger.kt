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
    val cron: String, // or a better structured Cron expression model
    override val createdAt: Instant,
    val updatedAt: Instant? = null
) : Trigger
