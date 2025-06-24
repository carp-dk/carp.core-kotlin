package dk.cachet.carp.analytics.infrastructure.trigger

import dk.cachet.carp.analytics.domain.trigger.*
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*

class DBBackedTriggerServiceTest {

    private val repo = MockTriggerRepository()
    private val service = DBBackedTriggerService(repo)
    private val studyId = UUID.randomUUID()
    private val workflowId = UUID.randomUUID()

    @Test
    fun create_and_get_trigger_works() = runTest {
        val trigger = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "Test", Clock.System.now())
        service.createTrigger(trigger)

        val retrieved = service.getTrigger(trigger.id)
        assertNotNull(retrieved)
        assertEquals(trigger.id, retrieved.id)
    }

    @Test
    fun update_and_delete_trigger_works() = runTest {
        val trigger = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "Test", Clock.System.now())
        service.createTrigger(trigger)

        val updated = trigger.copy(name = "Updated")
        service.updateTrigger(updated)

        assertEquals("Updated", service.getTrigger(trigger.id)?.name)

        assertTrue(service.deleteTrigger(trigger.id))
        assertNull(service.getTrigger(trigger.id))
    }

    @Test
    fun activation_lifecycle_creates_activation() = runTest {
        val trigger = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "Test", Clock.System.now())
        service.createTrigger(trigger)

        assertTrue(service.startTrigger(trigger.id, Clock.System.now()))
        assertTrue(service.getActivationsForTrigger(trigger.id).isNotEmpty())
    }

    @Test
    fun filter_triggers_by_study_and_workflow_works() = runTest {
        val trigger1 = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "One", Clock.System.now())
        val trigger2 = ManualTrigger(UUID.randomUUID(), studyId, UUID.randomUUID(), "Two", Clock.System.now())

        service.createTrigger(trigger1)
        service.createTrigger(trigger2)

        assertEquals(2, service.listTriggers(studyId).size)
        assertEquals(1, service.listByWorkflow(studyId, workflowId).size)
    }

    @Test
    fun record_activation_explicitly_works() = runTest {
        val trigger = ManualTrigger(UUID.randomUUID(), studyId, workflowId, "Test", Clock.System.now())
        service.createTrigger(trigger)

        val activation = TriggerActivation(UUID.randomUUID(), trigger.id, studyId, Clock.System.now(), null)
        service.recordActivation(activation)

        assertTrue(service.getActivationsForTrigger(trigger.id).contains(activation))
    }
}
