package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID

/**
 * A contract for submitting workflow execution jobs to a background job execution system.
 *
 * Implementations may use local coroutines (for development or testing) or remote/distributed
 * execution environments.
 *
 * Used by the [ExecutionOrchestratorService] to decouple workflow execution from orchestration logic.
 */
interface JobManager {
    /**
     * Submit a workflow execution job identified by [executionId] and the full [workflow] definition.
     *
     * This method is non-blocking and should offload execution to a worker mechanism.
     */
    fun submitWorkflowJob(executionId: UUID, workflow: Workflow)
}