package dk.cachet.carp.analytics.application.service

import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
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
     *
     * @return the result of execution, including status, outputs, and any downloadable artifacts.
     *
     * @throws IllegalArgumentException if the workflow ID is invalid or the user lacks access.
     */
    suspend fun executeWorkflow(workflowId: UUID): ExecutionResult

    /**
     * Execute a provided workflow definition without persisting it first.
     * Useful for ad-hoc or test executions.
     *
     * @return the result of execution, including status, outputs, and any downloadable artifacts.
     *
     * @throws IllegalArgumentException if the workflow is invalid or incomplete.
     */
    suspend fun executeWorkflowFromDefinition(workflow: Workflow): ExecutionResult

    /**
     * Retrieve a previously completed execution result.
     *
     * @return the execution result if it exists, or null.
     */
    suspend fun getExecutionResult(executionId: UUID): ExecutionResult?
}
