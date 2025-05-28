package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.analytics.application.execution.JobManager
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Clock
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DefaultExecutionOrchestratorServiceTest {

    private val exampleWorkflowId = UUID("00000000-0000-0000-0000-000000000123")
    private val exampleWorkflowMetadata = WorkflowMetadata(
        id = exampleWorkflowId,
        name = "Sleep Quality Analysis",
        version = Version(1, 0)
    )

    private val exampleWorkflow = Workflow(
        exampleWorkflowMetadata
    )

    private class MockWorkflowService(
        private val workflow: Workflow?
    ) : WorkflowService {

        override suspend fun getWorkflow(studyId: UUID, workflowId: UUID): Workflow? = workflow

        // Other functions can be left unimplemented or throw
        override suspend fun createWorkflow(studyId: UUID, workflow: Workflow) = throw NotImplementedError()
        override suspend fun updateWorkflow(studyId: UUID, workflowMetadata: WorkflowMetadata, updated: Workflow) = false
        override suspend fun deleteWorkflow(studyId: UUID, workflowId: UUID) = false
        override suspend fun listWorkflows(studyId: UUID): List<WorkflowMetadata> = emptyList()
    }

    private class MockJobManager : JobManager {
        var submittedWorkflow: Workflow? = null
        var submittedId: UUID? = null

        override fun submitWorkflowJob(executionId: UUID, workflow: Workflow) {
            submittedId = executionId
            submittedWorkflow = workflow
        }
    }

    private class DummyStudyDataService : StudyDataService {
        override suspend fun getCollectedData(
            studyId: UUID,
            studyDeploymentIds: Set<UUID>?,
            deviceRoleNames: Set<String>?,
            fields: Set<String>?,
            from: kotlinx.datetime.Instant?,
            to: kotlinx.datetime.Instant?,
            offsetDays: Int?
        ) = throw NotImplementedError()
    }

    @Test
    fun testLaunchWorkflowDelegatesCorrectly() = runTest {
        val workflow = exampleWorkflow
        val studyId = UUID.randomUUID()
        val executionId = UUID.randomUUID()
        val state = ExecutionState(
            executionId = executionId,
            status = ExecutionStatus.RUNNING,
            startedAt = Clock.System.now(),
            completedAt = null,
            workflowId = workflow.metadata.id,
            studyId = studyId
        )

        val mockWorkflowService = MockWorkflowService(workflow)
        val mockJobManager = MockJobManager()
        val service = DefaultExecutionOrchestratorService(
            mockWorkflowService,
            DummyStudyDataService(),
            mockJobManager
        )

        service.launchWorkflow(state, null)

        assertEquals(executionId, mockJobManager.submittedId)
        assertEquals(workflow, mockJobManager.submittedWorkflow)
    }
}
