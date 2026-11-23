package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.validation.ValidationResult
import dk.cachet.carp.analytics.domain.workflow.StepsLock
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Validates, stores and retrieves the analysis workflows of a study.
 */
interface WorkflowService : ApplicationService<WorkflowService, WorkflowService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event : IntegrationEvent<WorkflowService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Checks the workflow written in [workflowYaml] for [studyId] without storing it.
     *
     * Checks everything that can be known before running: step references, lint rules, the execution plan,
     * and whether the study protocol collects the data the workflow reads.
     */
    suspend fun validateWorkflow( studyId: UUID, workflowYaml: String ): ValidationResult

    /**
     * Validates the workflow written in [workflowYaml] and stores it for [studyId] when it has no errors.
     *
     * @return The stored workflow's metadata and any warnings, or the issues that stopped it from being stored.
     */
    suspend fun submitWorkflow( studyId: UUID, workflowYaml: String ): SubmitResult

    /**
     * Stores an already parsed [definition], the workflow with its environments, for [studyId].
     *
     * @return false when a workflow with the same id already exists.
     */
    suspend fun createWorkflow( studyId: UUID, definition: WorkflowDefinition ): Boolean

    /**
     * Stores [definition] as a new version of the existing workflow with the same id.
     *
     * @return false when no workflow with this id exists in [studyId].
     */
    suspend fun updateWorkflow( studyId: UUID, definition: WorkflowDefinition ): Boolean

    /**
     * Returns the workflow with [workflowId] in [studyId], with its environments,
     * at [version], or the latest version when null.
     *
     * @return null when the workflow or the version does not exist.
     */
    suspend fun getWorkflow( studyId: UUID, workflowId: UUID, version: Version? = null ): WorkflowDefinition?

    /**
     * Returns the metadata of the latest version of every workflow in [studyId].
     */
    suspend fun listWorkflows( studyId: UUID ): List<WorkflowMetadata>

    /**
     * Returns the metadata of every stored version of the workflow with [workflowId], oldest first.
     */
    suspend fun listWorkflowVersions( studyId: UUID, workflowId: UUID ): List<WorkflowMetadata>

    /**
     * Returns the step library versions pinned for the workflow with [workflowId] at [version],
     * or the latest version when null.
     *
     * @return null when the workflow or the version does not exist.
     */
    suspend fun getStepsLock( studyId: UUID, workflowId: UUID, version: Version? = null ): StepsLock?

    /**
     * Removes the workflow with [workflowId] and all its versions from [studyId]. Past runs and their results are kept.
     *
     * @return false when no such workflow exists.
     */
    suspend fun deleteWorkflow( studyId: UUID, workflowId: UUID ): Boolean
}
