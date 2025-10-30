package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker

/**
 * Invokes [WorkflowService] methods based on [WorkflowServiceRequest]s.
 */
object WorkflowServiceInvoker : ApplicationServiceInvoker<WorkflowService, WorkflowServiceRequest<*>>
{
    override suspend fun WorkflowServiceRequest<*>.invoke( service: WorkflowService ): Any? =
        when (this)
        {
            is WorkflowServiceRequest.CreateWorkflow ->
                service.createWorkflow(studyId, workflow)

            is WorkflowServiceRequest.UpdateWorkflow ->
                service.updateWorkflow(studyId, workflowMetadata, updated)

            is WorkflowServiceRequest.GetWorkflow ->
                service.getWorkflow(studyId, workflowId)

            is WorkflowServiceRequest.DeleteWorkflow ->
                service.deleteWorkflow(studyId, workflowId)

            is WorkflowServiceRequest.ListWorkflows ->
                service.listWorkflows(studyId)
        }
}
