package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.ExecutionRepository
import dk.cachet.carp.analytics.application.execution.ExecutionOrchestratorService
import dk.cachet.carp.analytics.domain.execution.BasicExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class DBBackedExecutionServiceTest {

    class MockExecutionRepository : ExecutionRepository {
        val savedStates = mutableListOf<ExecutionState>()
        val updatedStates = mutableListOf<ExecutionState>()
        val savedResults = mutableMapOf<UUID, ExecutionResult?>()
        var latestQuery: Triple<UUID, UUID?, Pair<Instant?, Instant?>>? = null

        override suspend fun saveState(state: ExecutionState): Boolean {
            savedStates += state
            return true
        }

        override suspend fun updateState(state: ExecutionState): Boolean {
            updatedStates += state
            return true
        }

        override suspend fun getState(executionId: UUID): ExecutionState? =
            (savedStates + updatedStates).find { it.executionId == executionId }

        override suspend fun getLatestStatus(workflowId: UUID): ExecutionState? =
            (savedStates + updatedStates)
                .filter { it.workflowId == workflowId }
                .maxByOrNull { it.startedAt }

        override suspend fun findByStudy(
            studyId: UUID,
            workflowId: UUID?,
            from: Instant?,
            to: Instant?
        ): List<ExecutionState> {
            latestQuery = Triple(studyId, workflowId, from to to)
            return (savedStates + updatedStates).filter {
                it.studyId == studyId &&
                        (workflowId == null || it.workflowId == workflowId) &&
                        (from == null || it.startedAt >= from) &&
                        (to == null || it.startedAt <= to)
            }
        }

        override suspend fun saveResult(result: ExecutionResult): Boolean {
            savedResults[result.executionId] = result
            return true
        }

        override suspend fun getResult(executionId: UUID): ExecutionResult? =
            savedResults[executionId]
    }


    class MockOrchestrator : ExecutionOrchestratorService {
        val launched = mutableListOf<ExecutionState>()

        override suspend fun launchWorkflow(executionState: ExecutionState, workflow: Workflow?) {
            launched += executionState
        }
    }

    private val repo = MockExecutionRepository()
    private val orchestrator = MockOrchestrator()
    private val service = DBBackedExecutionService(repo, orchestrator)

    @Test
    fun testExecuteWorkflowSavesStateAndCallsOrchestrator() = runTest {
        val studyId = UUID.randomUUID()
        val workflowId = UUID.randomUUID()

        val state = service.executeWorkflow(studyId, workflowId)

        assertEquals(1, repo.savedStates.size)
        assertEquals(state.executionId, repo.savedStates.first().executionId)
        assertEquals(state, orchestrator.launched.first())
    }

    @Test
    fun testGetExecutionStateDelegatesToRepo() = runTest {
        val state = ExecutionState(
            UUID.randomUUID(), ExecutionStatus.RUNNING,
            Clock.System.now(), null,
            UUID.randomUUID(), UUID.randomUUID()
        )
        repo.saveState(state)

        val retrieved = service.getExecutionState(state.executionId)
        assertEquals(state, retrieved)
    }

    @Test
    fun testGetExecutionResultDelegatesToRepo() = runTest {
        val executionId = UUID.randomUUID()
        val result = BasicExecutionResult(
            executionId = executionId,
            status = ExecutionStatus.COMPLETED,
            outputs = emptyList(),
            artifacts = emptyList()
        )

        repo.saveResult(result)

        val retrieved = service.getExecutionResult(executionId)
        assertEquals(result, retrieved)
    }

    @Test
    fun testFindExecutionsDelegatesWithFilters() = runTest {
        val studyId = UUID.randomUUID()
        val workflowId = UUID.randomUUID()
        val from = Clock.System.now()
        val to = from.plus(1000.milliseconds)

        service.findExecutions(studyId, workflowId, from, to)

        val (sid, wid, range) = repo.latestQuery!!
        assertEquals(studyId, sid)
        assertEquals(workflowId, wid)
        assertEquals(from, range.first)
        assertEquals(to, range.second)
    }

    @Test
    fun testGetLatestExecutionStatusReturnsMostRecent() = runTest {
        val workflowId = UUID.randomUUID()
        val studyId = UUID.randomUUID()

        val e1 = ExecutionState(UUID.randomUUID(), ExecutionStatus.RUNNING, Instant.fromEpochMilliseconds(1000), null, workflowId, studyId)
        val e2 = ExecutionState(UUID.randomUUID(), ExecutionStatus.COMPLETED, Instant.fromEpochMilliseconds(2000), null, workflowId, studyId)

        repo.saveState(e1)
        repo.saveState(e2)

        val latest = service.getLatestExecutionStatus(studyId, workflowId)
        assertEquals(e2.executionId, latest?.executionId)
    }
}
