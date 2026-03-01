package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class ExecutionReport(
    val runId: UUID,
    val status: ExecutionStatus,
    val startTime: Instant?,
    val finishTime: Instant?,
    val stepResults: List<StepRunResult>
)
