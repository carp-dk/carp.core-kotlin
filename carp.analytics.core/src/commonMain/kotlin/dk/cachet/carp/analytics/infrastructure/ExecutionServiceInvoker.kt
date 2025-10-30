package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker

/**
 * Invokes [ExecutionService] methods based on [ExecutionServiceRequest]s.
 */
object ExecutionServiceInvoker : ApplicationServiceInvoker<ExecutionService, ExecutionServiceRequest<*>>
{
    override suspend fun ExecutionServiceRequest<*>.invoke( service: ExecutionService ): Any? =
        when (this) {
            is ExecutionServiceRequest.ExecuteWorkflow ->
                service.executeWorkflow(studyId, workflowId)

            is ExecutionServiceRequest.ExecuteWorkflowFromDefinition ->
                service.executeWorkflowFromDefinition(studyId, workflow)

            is ExecutionServiceRequest.GetExecutionState ->
                service.getExecutionState(executionId)

            is ExecutionServiceRequest.GetExecutionResult ->
                service.getExecutionResult(executionId)

            is ExecutionServiceRequest.FindExecutions ->
                service.findExecutions(studyId, workflowId, from, to)

            is ExecutionServiceRequest.GetLatestExecutionStatus ->
                service.getLatestExecutionStatus(studyId, workflowId)
        }
}
