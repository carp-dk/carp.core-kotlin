package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.execution.ExecutionOrchestratorService
import dk.cachet.carp.analytics.application.execution.JobManager
import dk.cachet.carp.analytics.application.execution.WorkflowInjector
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencies
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.data.application.StudyDataService

class DefaultExecutionOrchestratorService(
    private val workflowService: WorkflowService,
    private val studyDataService: StudyDataService,
    private val jobManager: JobManager,
) : ExecutionOrchestratorService {

    override suspend fun launchWorkflow(executionState: ExecutionState, workflow: Workflow?){
        val actualWorkflow = workflow ?: workflowService.getWorkflow(
            executionState.studyId,
            executionState.workflowId
        ) ?: throw IllegalArgumentException("Workflow not found.")

        val dependencies = mapOf(
            RuntimeDependencies.StudyDataService to studyDataService,
            RuntimeDependencies.DataRegistry to DataRegistry()
        )

        WorkflowInjector.inject(actualWorkflow, dependencies)
        jobManager.submitWorkflowJob(executionState.executionId, actualWorkflow)

    }


}
