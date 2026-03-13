package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.plan.PlannedStep
import dk.cachet.carp.analytics.application.runtime.CommandResult
import dk.cachet.carp.analytics.application.execution.workspace.ExecutionWorkspace
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant


/**
 * Records logs produced by step execution.
 *
 * Handles:
 * - Combining stdout and stderr
 * - Creating log files with metadata
 * - Returning ResourceRef for log location
 *
 * Can be extended with different strategies:
 * - FileSystem (current)
 * - CloudWatch (future)
 * - S3-backed (future)
 * - Datadog (future)
 */
interface StepLogRecorder {
    /**
     * Record logs from a command execution result.
     *
     * @param step The step that was executed
     * @param result The execution result containing stdout/stderr
     * @param workspace The workspace where logs are stored
     * @return ResourceRef pointing to the log file, or null if no logs to record
     */
    fun recordLogs(
        step: PlannedStep,
        result: CommandResult,
        workspace: ExecutionWorkspace
    ): ResourceRef?
}

/**
 * Metadata about recorded logs.
 *
 * Used internally by implementations for tracking.
 */
data class LogRecord(
    val stepId: UUID,
    val location: ResourceRef,
    val hasStdout: Boolean,
    val hasStderr: Boolean,
    val recordedAt: Instant
)