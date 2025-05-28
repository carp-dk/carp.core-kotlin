package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.StudyDataService
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*

class LocalJobManagerTest {

    private lateinit var executionService: ExecutionService
    private lateinit var studyDataService: StudyDataService
    private lateinit var repository: SQLiteExecutionRepository
    private lateinit var manager: LocalJobManager

    private val executionId = UUID.randomUUID()
    private val studyId = UUID.randomUUID()
    private val workflowId = UUID.randomUUID()

    private lateinit var initialState: ExecutionState

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)

        executionService = mockk()
        studyDataService = mockk()
        repository = mockk(relaxed = true)

        manager = LocalJobManager(executionService, studyDataService, repository)

        initialState = ExecutionState(
            executionId = executionId,
            status = ExecutionStatus.QUEUED,
            startedAt = Clock.System.now(),
            completedAt = null,
            workflowId = workflowId,
            studyId = studyId
        )

        coEvery { executionService.getExecutionState(executionId) } returns initialState
    }

    @Test
    fun `should complete job successfully`() = runTest {
        val workflow = Workflow(
            metadata = WorkflowMetadata(
                name = "Test Workflow",
                description = "Simple test workflow",
                id = workflowId
            )
        )

        manager.submitWorkflowJob(executionId, workflow)
        delay(300)

        coVerify { repository.updateState(match { it.status == ExecutionStatus.RUNNING }) }
        coVerify { repository.updateState(match { it.status == ExecutionStatus.COMPLETED }) }
        coVerify { repository.saveResult(match { it.executionId == executionId && it.status == ExecutionStatus.COMPLETED }) }
    }

    @Test
    fun `should handle execution failure`() = runTest {
        val faultyWorkflow = mockk<Workflow>()
        every { faultyWorkflow.metadata } returns WorkflowMetadata("Failing", "Will throw", workflowId)
        every { faultyWorkflow.getComponents() } throws RuntimeException("Simulated failure")

        manager.submitWorkflowJob(executionId, faultyWorkflow)
        delay(300)

        coVerify { repository.updateState(match { it.status == ExecutionStatus.FAILURE }) }
    }
}
