package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant


@Serializable
data class StepRunResult(
    val stepId: UUID,
    val status: ExecutionStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val failure: StepFailure?,
    val outputs: List<ExecutionOutputRef>?
)
