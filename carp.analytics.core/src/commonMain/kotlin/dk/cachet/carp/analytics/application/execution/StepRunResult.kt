package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StepRunResult(
    val stepId: UUID,
    val status: ExecutionStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val failure: StepFailure?,
    val outputs: List<OutputRef>? = emptyList(),
    val detail: StepRunDetail? = null
)
