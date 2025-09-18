package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID

interface WorkflowRepository {
    suspend fun create(metadata: WorkflowMetadata): Boolean
    suspend fun update(metadata: WorkflowMetadata): Boolean
    suspend fun findById(studyId: UUID, workflowId: UUID): WorkflowMetadata?
    suspend fun delete(studyId: UUID, workflowId: UUID): Boolean
    suspend fun listByStudyId(studyId: UUID): List<WorkflowMetadata>
}