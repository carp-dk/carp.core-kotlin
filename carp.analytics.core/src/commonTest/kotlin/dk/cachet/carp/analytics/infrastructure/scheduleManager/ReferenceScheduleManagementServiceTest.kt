package dk.cachet.carp.analytics.infrastructure.scheduleManager

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.application.TriggerRepository
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.analytics.domain.trigger.CronExpression
import dk.cachet.carp.analytics.domain.trigger.ScheduledTrigger
import dk.cachet.carp.analytics.domain.trigger.Trigger
import dk.cachet.carp.analytics.domain.trigger.TriggerActivation
import dk.cachet.carp.analytics.domain.workflow.Workflow
import kotlinx.datetime.Instant
import kotlin.test.*
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest

class ReferenceScheduleManagementServiceTest {

    private val now = Instant.parse("2025-06-18T10:00:00Z")
    private val studyId = UUID.randomUUID()
    private val workflowId = UUID.randomUUID()

    @Test
    fun triggers_fire_when_due() = runTest {
        val trigger = ScheduledTrigger(UUID.randomUUID(), studyId, workflowId, "Test", CronExpression("0 10 * * *"), now)


        val repo = object : TriggerRepository {
            override suspend fun create(trigger: Trigger) = trigger
            override suspend fun update(trigger: Trigger) = trigger
            override suspend fun delete(triggerId: UUID) = true
            override suspend fun get(triggerId: UUID) = null
            override suspend fun list(studyId: UUID) = emptyList<Trigger>()
            override suspend fun listByWorkflow(studyId: UUID, workflowId: UUID) = emptyList<Trigger>()
            override suspend fun startTrigger(triggerId: UUID, startedAt: Instant) = true
            override suspend fun recordActivation(activation: TriggerActivation) = true
            override suspend fun getActivationsForStudy(studyId: UUID) = emptyList<TriggerActivation>()
            override suspend fun getActivationsForTrigger(triggerId: UUID) = emptyList<TriggerActivation>()
            override suspend fun getAllScheduled() = listOf(trigger)
            override suspend fun getLatestActivationForTrigger(triggerId: UUID) = null
            override suspend fun addActivation(activation: TriggerActivation) {}
            override suspend fun endTrigger(triggerId: UUID) = true
        }

        var executed = false

        val execService = object : ExecutionService {
            override suspend fun executeWorkflow(studyId: UUID, workflowId: UUID) =
                ExecutionState(
                    executionId = UUID.randomUUID(),
                    workflowId = workflowId,
                    status = ExecutionStatus.RUNNING,
                    startedAt = Instant.fromEpochMilliseconds(1642505045000),
                    completedAt = null,
                    studyId = studyId
                ).also { executed = true }


            override suspend fun executeWorkflowFromDefinition(studyId: UUID, workflow: Workflow) =
                ExecutionState(
                    executionId = UUID.randomUUID(),
                    workflowId = workflowId,
                    status = ExecutionStatus.RUNNING,
                    startedAt = Instant.fromEpochMilliseconds(1642505045000),
                    completedAt = null,
                    studyId = studyId
                )

            override suspend fun getExecutionState(executionId: UUID) = null

            override suspend fun getExecutionResult(executionId: UUID) = null

            override suspend fun findExecutions(studyId: UUID, workflowId: UUID?, from: Instant?, to: Instant?) = emptyList<ExecutionState>()
            override suspend fun getLatestExecutionStatus(studyId: UUID, workflowId: UUID?) = null
        }

        val service = ReferenceScheduleManagementService(repo, execService)
        val result = service.evaluateDueTriggers(now)

        assertTrue(result)
        assertTrue(executed)
    }
}
