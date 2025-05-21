package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.application.WorkflowRepository
import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.analytics.application.WorkflowStorageService
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID

class DBBackedWorkflowService(
    private val repository: WorkflowRepository,
    private val storage: WorkflowStorageService
) : WorkflowService {

    override suspend fun createWorkflow(studyId: UUID, workflow: Workflow): Boolean {
        val path = storage.writeWorkflowToFile(workflow)
        val metadata = StoredWorkflowMetadata.fromMetadata(workflow.metadata, studyId, path)
        return repository.create(metadata)
    }

    override suspend fun updateWorkflow(studyId: UUID, workflowMetadata: WorkflowMetadata, updated: Workflow): Boolean {
        val path = storage.writeWorkflowToFile(updated)
        val metadata = StoredWorkflowMetadata.fromMetadata(workflowMetadata, studyId, path)
        return repository.update(metadata)
    }

    override suspend fun getWorkflow(studyId: UUID, workflowId: UUID): Workflow? {
        val metadata = repository.findById(studyId, workflowId) ?: return null
        return storage.readWorkflowFromFile(metadata.filePath)
    }

    override suspend fun deleteWorkflow(studyId: UUID, workflowId: UUID): Boolean {
        return repository.delete(studyId, workflowId)
    }

    override suspend fun listWorkflows(studyId: UUID): List<WorkflowMetadata> {
        return repository.listByStudyId(studyId).map { it.toWorkflowMetadata() }
    }
}
