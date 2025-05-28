package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.ExecutionRepository
import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.application.execution.ExecutionOrchestratorService
import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * A database-backed implementation of [ExecutionService].
 *
 * This service handles registration and coordination of workflow executions by:
 * - Creating [ExecutionState] records for tracking execution lifecycle.
 * - Delegating actual workflow launches to the [ExecutionOrchestratorService].
 * - Exposing APIs to query past executions and their results.
 *
 * This implementation persists state using the injected [ExecutionRepository],
 * and is designed for use in production or testing environments with full tracking support.
 */
class DBBackedExecutionService(
    private val repo: ExecutionRepository,
    private val orchestrator: ExecutionOrchestratorService
) : ExecutionService {

    /**
     * Start a workflow by study/workflow ID and create an associated execution record.
     *
     * This is the main entrypoint for launching a pre-registered workflow.
     * Triggers asynchronous execution via [ExecutionOrchestratorService].
     */
    override suspend fun executeWorkflow(studyId: UUID, workflowId: UUID): ExecutionState {
        val id = UUID.randomUUID()
        val state = ExecutionState(id,
        ExecutionStatus.RUNNING,
        Clock.System.now(),
        null,
        workflowId,
        studyId)
        repo.saveState(state)

        // Launch the workflow (includes dependency injection and job submission)
        orchestrator.launchWorkflow(state, null)


        return state
    }

    /**
     * Start a workflow using an inline [Workflow] definition.
     *
     * Allows ad-hoc workflows to be executed without first registering them.
     */
    override suspend fun executeWorkflowFromDefinition(studyId: UUID, workflow: Workflow): ExecutionState {
        val id = UUID.randomUUID()
        val state = ExecutionState(id,
            ExecutionStatus.RUNNING,
            Clock.System.now(),
            null,
            workflow.metadata.id,
            studyId)
        repo.saveState(state)

        // Launch the workflow (includes dependency injection and job submission)
        orchestrator.launchWorkflow(state, workflow)

        return state
    }

    /**
     * Retrieve the current [ExecutionState] for a given execution.
     */
    override suspend fun getExecutionState(executionId: UUID): ExecutionState? =
        repo.getState(executionId)

    /**
     * Retrieve the final result of a completed execution, if available.
     */
    override suspend fun getExecutionResult(executionId: UUID): ExecutionResult? =
        repo.getResult(executionId)

    /**
     * Find all executions by study and optional workflow ID and/or time window.
     */
    override suspend fun findExecutions(
        studyId: UUID,
        workflowId: UUID?,
        from: Instant?,
        to: Instant?
    ): List<ExecutionState> = repo.findByStudy(studyId, workflowId, from, to)

    /**
     * Get the most recent execution for the specified workflow in a study.
     */
    override suspend fun getLatestExecutionStatus(
        studyId: UUID,
        workflowId: UUID?
    ): ExecutionState? {
        return repo.findByStudy(studyId, workflowId).maxByOrNull { it.startedAt }
    }
}
