package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker


/**
 * Invokes [ExecutionService] methods based on [ExecutionServiceRequest]s.
 */
object ExecutionServiceInvoker : ApplicationServiceInvoker<ExecutionService, ExecutionServiceRequest<*>>
{
    override suspend fun ExecutionServiceRequest<*>.invoke( service: ExecutionService ): Any? =
        when ( this )
        {
            is ExecutionServiceRequest.ExecuteWorkflow -> service.executeWorkflow( studyId, workflowId )
            is ExecutionServiceRequest.ExecuteWorkflowFromDefinition ->
                service.executeWorkflowFromDefinition( studyId, definition )
            is ExecutionServiceRequest.CancelExecution -> service.cancelExecution( studyId, executionId )
            is ExecutionServiceRequest.GetExecutionState -> service.getExecutionState( studyId, executionId )
            is ExecutionServiceRequest.GetExecutionResult -> service.getExecutionResult( studyId, executionId )
            is ExecutionServiceRequest.FindExecutions -> service.findExecutions( studyId, workflowId, from, to, limit )
            is ExecutionServiceRequest.GetParticipantResults ->
                service.getParticipantResults( studyDeploymentId, since )
        }
}
