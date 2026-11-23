package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker


/**
 * Invokes [WorkflowService] methods based on [WorkflowServiceRequest]s.
 */
object WorkflowServiceInvoker : ApplicationServiceInvoker<WorkflowService, WorkflowServiceRequest<*>>
{
    override suspend fun WorkflowServiceRequest<*>.invoke( service: WorkflowService ): Any? =
        when ( this )
        {
            is WorkflowServiceRequest.ValidateWorkflow -> service.validateWorkflow( studyId, workflowYaml )
            is WorkflowServiceRequest.SubmitWorkflow -> service.submitWorkflow( studyId, workflowYaml )
            is WorkflowServiceRequest.CreateWorkflow -> service.createWorkflow( studyId, definition )
            is WorkflowServiceRequest.UpdateWorkflow -> service.updateWorkflow( studyId, definition )
            is WorkflowServiceRequest.GetWorkflow -> service.getWorkflow( studyId, workflowId, version )
            is WorkflowServiceRequest.ListWorkflows -> service.listWorkflows( studyId )
            is WorkflowServiceRequest.ListWorkflowVersions -> service.listWorkflowVersions( studyId, workflowId )
            is WorkflowServiceRequest.GetStepsLock -> service.getStepsLock( studyId, workflowId, version )
            is WorkflowServiceRequest.DeleteWorkflow -> service.deleteWorkflow( studyId, workflowId )
        }
}
