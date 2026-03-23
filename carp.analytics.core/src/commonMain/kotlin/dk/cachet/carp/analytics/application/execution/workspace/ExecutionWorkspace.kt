package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.INPUTS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.LOGS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.OUTPUTS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.STEPS_DIR
import dk.cachet.carp.common.application.UUID

/**
 * Represents a run-scoped workspace.
 *
 * This is a pure, deterministic workspace model that provides logical path construction
 * without any filesystem access or IO operations. The [executionRoot] is the workspace
 * root directory (typically an absolute filesystem path), and helper methods return
 * relative subpaths inside that root.
 *
 * @param runId Unique identifier for this execution run
 * @param executionRoot Workspace root identifier (usually an absolute filesystem path)
 */
data class ExecutionWorkspace(
    val runId: UUID,
    val executionRoot: String
)
{
    /**
     * Returns the step directory path for the given step ID.
     *
     * @param stepId The step identifier UUID.
     * @return Relative path "steps/{stepMetadata}"
     */
    fun stepDir( stepId: UUID ): String
    {
        return "$STEPS_DIR/$stepId"
    }

    /**
     * Returns the step inputs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID.
     * @return Relative path "steps/{stepMetadata}/inputs"
     */
    fun stepInputsDir( stepId: UUID ): String
    {
        return "$STEPS_DIR/$stepId/$INPUTS_DIR"
    }

    /**
     * Returns the step outputs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID.
     * @return Relative path "steps/{stepMetadata}/outputs"
     */
    fun stepOutputsDir( stepId: UUID ): String
    {
        return "$STEPS_DIR/$stepId/$OUTPUTS_DIR"
    }

    /**
     * Returns the step logs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID.
     * @return Relative path "steps/{stepMetadata}/logs"
     */
    fun stepLogsDir( stepId: UUID ): String
    {
        return "$STEPS_DIR/$stepId/$LOGS_DIR"
    }
}
