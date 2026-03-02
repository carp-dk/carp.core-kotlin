package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.analytics.application.plan.ExecutionPlan
import dk.cachet.carp.common.application.UUID

/**
 * Execution-stage port for workspace materialization.
 *
 * This interface defines the contract for workspace management operations
 * without containing any filesystem logic. Implementations are responsible
 * for the actual materialization of workspace structures.
 */
interface WorkspaceManager
{
    /**
     * Creates a new execution workspace for the given execution plan and run ID.
     *
     * @param plan The execution plan containing workflow and step information
     * @param runId Unique identifier for the execution run
     * @return A new ExecutionWorkspace instance
     */
    fun create(plan: ExecutionPlan, runId: UUID): ExecutionWorkspace

    /**
     * Prepares the directory structure for a specific step within the workspace.
     *
     * This should ensure that the step's directory structure (inputs, outputs, logs)
     * is ready for use.
     *
     * @param workspace The execution workspace
     * @param stepId The step identifier UUID
     */
    fun prepareStepDirectories(workspace: ExecutionWorkspace, stepId: UUID)
}


