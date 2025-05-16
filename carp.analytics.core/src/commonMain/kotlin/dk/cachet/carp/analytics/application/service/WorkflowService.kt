package dk.cachet.carp.analytics.application.service

import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * Service for creating, updating, and managing analytic workflows.
 */
interface WorkflowService : ApplicationService<WorkflowService, WorkflowService.Event>
{
    companion object { val API_VERSION = ApiVersion(1, 0) }

    @Serializable
    sealed class Event : IntegrationEvent<WorkflowService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }

    /**
     * Create a new workflow for the specified [studyId].
     *
     * @return true if the workflow was successfully created, false otherwise (e.g., duplicate or invalid).
     */
    suspend fun createWorkflow(studyId: UUID, workflow: Workflow): Boolean

    /**
     * Update an existing workflow identified by [workflowMetadata.id] for the given [studyId].
     *
     * @throws NoSuchElementException if no such workflow exists.
     */
    suspend fun updateWorkflow(studyId: UUID, workflowMetadata: WorkflowMetadata, updated: Workflow): Boolean

    /**
     * Get a workflow for the given [studyId] and [workflowId].
     *
     * @return The workflow, or null if not found or access is denied.
     */
    suspend fun getWorkflow(studyId: UUID, workflowId: UUID): Workflow?

    /**
     * Delete a workflow from a study.
     *
     * @return true if deleted, false otherwise.
     */
    suspend fun deleteWorkflow(studyId: UUID, workflowId: UUID): Boolean

    /**
     * List summaries of all workflows in a given study.
     */
    suspend fun listWorkflows(studyId: UUID): List<WorkflowMetadata>
}
