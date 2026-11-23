package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.data.ArtifactRecord
import dk.cachet.carp.analytics.infrastructure.execution.EnvironmentExecutionLogs
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import kotlin.time.Instant


/**
 * The record of a run that has ended.
 *
 * @property planId The execution plan that was run.
 * @property startedAt Null when the run ended before it started, e.g., when it was cancelled while pending.
 * @property status [ExecutionStatus.SUCCEEDED], [ExecutionStatus.FAILED] or [ExecutionStatus.CANCELLED].
 * @property stepResults One result for every planned step.
 * @property issues Run-level issues, empty when there were none.
 * @property artifacts Every artefact the run produced, with where to fetch it.
 */
@Serializable
data class ExecutionReport(
    val executionId: UUID,
    val planId: UUID,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val status: ExecutionStatus,
    val stepResults: List<StepRunResult>,
    val issues: List<ExecutionIssue> = emptyList(),
    val artifacts: List<ArtifactRecord> = emptyList(),
    // Runtime type in the API. To be replaced or dropped as part of CORE-18.
    val environmentLogs: EnvironmentExecutionLogs = EnvironmentExecutionLogs()
)
