package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutorState
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * Service for executing workflows and retrieving results.
 */
interface ExecutionService : ApplicationService<ExecutionService, ExecutionService.Event>
{
    companion object { val API_VERSION = ApiVersion(1, 0) }

    @Serializable
    sealed class Event : IntegrationEvent<ExecutionService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }

    /**
     * Execute a stored workflow identified by [workflowId].
     */
    suspend fun executeWorkflow( studyId: UUID, workflowId: UUID ): ExecutorState

    /**
     * Execute a provided workflow definition without persisting it first.
     */
    suspend fun executeWorkflowFromDefinition( studyId: UUID, workflow: Workflow ): ExecutorState

    /**
     * Retrieve the current state (running/completed) of a specific execution.
     */
    suspend fun getExecutionState( executionId: UUID ): ExecutorState?

    /**
     * Retrieve the result of a completed execution.
     */
    suspend fun getExecutionResult( executionId: UUID ): ExecutionResult?

    /**
     * Query past execution jobs by study/workflow/time range.
     */
    suspend fun findExecutions(
        studyId: UUID,
        workflowId: UUID? = null,
        from: Instant? = null,
        to: Instant? = null
    ): List<ExecutorState>

    /**
     * Get the most recent execution (either running or completed) for a given workflow.
     */
    suspend fun getLatestExecutionStatus( studyId: UUID, workflowId: UUID? ): ExecutorState?
}
