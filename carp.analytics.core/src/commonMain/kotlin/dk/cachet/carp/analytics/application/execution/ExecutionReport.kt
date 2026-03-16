package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.infrastructure.execution.EnvironmentExecutionLogs
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * The complete record of a finished execution run.
 *
 * @param runId        Unique identifier for this run.
 * @param planId       The [dk.cachet.carp.analytics.application.plan.ExecutionPlan.planId] that was executed.
 * @param startedAt    Wall-clock time the run began; null if it never started.
 * @param finishedAt   Wall-clock time the run completed; null if it never finished.
 * @param status       Terminal status of the overall run.
 * @param stepResults  Result record for every planned step.
 * @param issues       Run-level issues encountered during orchestration (empty when clean).
 */
@Serializable
data class ExecutionReport(
    val runId: UUID,
    val planId: UUID,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val status: ExecutionStatus,
    val stepResults: List<StepRunResult>,
    val issues: List<ExecutionIssue> = emptyList(),
    val environmentLogs: EnvironmentExecutionLogs = EnvironmentExecutionLogs()
)
