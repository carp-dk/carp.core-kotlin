package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.workflow.Workflow

interface WorkflowStorageService {
    suspend fun writeWorkflowToFile(workflow: Workflow): String
    suspend fun readWorkflowFromFile(filePath: String): Workflow
}
