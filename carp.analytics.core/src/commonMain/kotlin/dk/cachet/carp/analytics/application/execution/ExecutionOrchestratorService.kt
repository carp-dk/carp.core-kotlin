package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * Defines the contract for services that can orchestrate the execution of analytic workflows.
 *
 * The orchestrator is responsible for preparing the workflow for execution (e.g., injecting dependencies),
 * and dispatching it to the appropriate execution engine or job manager.
 */
interface ExecutionOrchestratorService {
    suspend fun launchWorkflow(executionState: ExecutionState, workflow: Workflow?)
}