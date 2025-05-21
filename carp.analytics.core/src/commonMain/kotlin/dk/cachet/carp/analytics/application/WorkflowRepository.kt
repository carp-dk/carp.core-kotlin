package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.infrastructure.workflow.StoredWorkflowMetadata
import dk.cachet.carp.common.application.UUID

interface WorkflowRepository {
    suspend fun create(metadata: StoredWorkflowMetadata): Boolean
    suspend fun update(metadata: StoredWorkflowMetadata): Boolean
    suspend fun findById(studyId: UUID, workflowId: UUID): StoredWorkflowMetadata?
    suspend fun delete(studyId: UUID, workflowId: UUID): Boolean
    suspend fun listByStudyId(studyId: UUID): List<StoredWorkflowMetadata>
}