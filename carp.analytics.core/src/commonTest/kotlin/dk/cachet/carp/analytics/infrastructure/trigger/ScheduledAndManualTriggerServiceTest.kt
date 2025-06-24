package dk.cachet.carp.analytics.infrastructure.trigger

import dk.cachet.carp.analytics.domain.trigger.*
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*


class ScheduledAndManualTriggerServiceTest {

    private val repo = MockTriggerRepository()
    private val service = DBBackedTriggerService(repo)
    private val studyId = UUID.randomUUID()
    private val workflowId = UUID.randomUUID()

    @Test
    fun manual_trigger_can_be_created_and_fired() = runTest {
        val trigger = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "ManualTest", Clock.System.now())
        service.createTrigger(trigger)

        assertTrue(service.startTrigger(trigger.id, Clock.System.now()))
        assertTrue(service.getActivationsForTrigger(trigger.id).isNotEmpty())
    }

    @Test
    fun scheduled_trigger_can_be_created_and_next_run_calculated() = runTest {
        val cron = CronExpression("0 9 * * *") // Daily at 9 AM
        val trigger = ScheduledTrigger(UUID.randomUUID(), studyId, workflowId, "ScheduledTest", cron, Clock.System.now())
        service.createTrigger(trigger)

        val retrieved = service.getTrigger(trigger.id) as? ScheduledTrigger
        assertNotNull(retrieved)

        val fixedTime = Instant.parse("2025-06-18T07:00:00Z")
        val nextRun = retrieved.getNextScheduledTime(fixedTime)

        assertNotNull(nextRun)
        assertEquals("2025-06-18T09:00", nextRun.toString())
    }
}
