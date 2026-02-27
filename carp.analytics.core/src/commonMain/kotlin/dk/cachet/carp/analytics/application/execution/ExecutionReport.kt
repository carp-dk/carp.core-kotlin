package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

@Serializable
data class ExecutionReport(
    val runId: UUID,
    val status: ExecutionStatus,
    val stepResults: List<StepRunResult>
)
