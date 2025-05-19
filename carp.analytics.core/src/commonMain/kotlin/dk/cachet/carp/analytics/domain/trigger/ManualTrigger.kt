package dk.cachet.carp.analytics.domain.trigger

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ManualTrigger")
data class ManualTrigger(
    override val id: UUID,
    override val studyId: UUID,
    override val workflowId: UUID,
    override val name: String,
    override val createdAt: Instant,
) : Trigger
