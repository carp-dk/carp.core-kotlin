package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.analytics.application.plan.ExecutionPlan
import dk.cachet.carp.common.application.UUID

/**
 * Execution-stage port for workspace materialization and management.
 *
 * This interface defines the contract for workspace management operations
 * without containing any filesystem logic. Implementations are responsible
 * for the actual materialization and clean-up of workspace structures.
 *
 * The workspace is **run-scoped**: each execution run gets its own isolated
 * workspace directory structure to prevent cross-run contamination.
 */
interface WorkspaceManager
{
    /**
     * Creates a new execution workspace for the given execution plan and run ID.
     *
     * This operation materializes the workspace directory structure on the filesystem
     * and returns a handle to reference the workspace in subsequent operations.
     *
     * The workspace layout created:
     * ```
     * baseRoot/
     *   {runId}/                  # executionRoot
     *     steps/                  # Created for consistency
     *       {stepId}/
     *         inputs/
     *         outputs/
     *         logs/
     * ```
     *
     * @param plan The execution plan containing workflow and step information
     * @param runId Unique identifier for the execution run
     * @return A new ExecutionWorkspace instance
     * @throws IllegalStateException if directory creation fails
     */
    fun create( plan: ExecutionPlan, runId: UUID ): ExecutionWorkspace

    /**
     * Prepares the directory structure for a specific step within the workspace.
     *
     * This should ensure that the step's directory structure (inputs, outputs, logs)
     * is ready for use before the step executes.
     *
     * Directory structure created:
     * ```
     * {executionRoot}/steps/{stepId}/
     *   inputs/                  # For input data
     *   outputs/                 # For step outputs
     *   logs/                    # For execution logs
     * ```
     *
     * @param workspace The execution workspace
     * @param stepId The step identifier UUID
     * @throws IllegalStateException if directory creation fails
     */
    fun prepareStepDirectories( workspace: ExecutionWorkspace, stepId: UUID )

    /**
     * Cleans up the workspace after execution completes.
     *
     * This operation safely removes the entire workspace directory tree.
     * If the workspace directory doesn't exist, this method completes silently
     *
     * @param workspace The execution workspace to clean up
     * @return true if clean-up succeeded, false if clean-up was not supported
     */
    fun cleanup( workspace: ExecutionWorkspace ): Boolean = false

    /**
     * Returns the absolute path string of the step's working directory within this workspace.
     *
     * This allows infrastructure runners to resolve a concrete on-disk path without
     * needing a separate base-root parameter — the manager already owns that knowledge.
     *
     * @param workspace The execution workspace produced by [create].
     * @param stepId    The step whose working directory should be resolved.
     * @return Absolute path string, or null if this manager cannot resolve filesystem paths
     *         (e.g. a stub or in-memory implementation).
     */
    fun resolveStepWorkingDir( workspace: ExecutionWorkspace, stepId: UUID ): String? = null
}
