package dk.cachet.carp.analytics.infrastructure.trigger

import dk.cachet.carp.analytics.application.TriggerRepository
import dk.cachet.carp.analytics.domain.trigger.*
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

class MockTriggerRepository : TriggerRepository {
    private val triggers = mutableMapOf<UUID, Trigger>()
    private val activations = mutableListOf<TriggerActivation>()

    override suspend fun create(trigger: Trigger): Trigger {
        triggers[trigger.id] = trigger
        return trigger
    }

    override suspend fun update(trigger: Trigger): Trigger {
        triggers[trigger.id] = trigger
        return trigger
    }

    override suspend fun delete(triggerId: UUID): Boolean = triggers.remove(triggerId) != null

    override suspend fun get(triggerId: UUID): Trigger? = triggers[triggerId]

    override suspend fun list(studyId: UUID): List<Trigger> =
        triggers.values.filter { it.studyId == studyId }

    override suspend fun listByWorkflow(studyId: UUID, workflowId: UUID): List<Trigger> =
        triggers.values.filter { it.studyId == studyId && it.workflowId == workflowId }

    override suspend fun startTrigger(triggerId: UUID, startedAt: Instant): Boolean {
        val trigger = triggers[triggerId] ?: return false
        activations += TriggerActivation(UUID.randomUUID(), triggerId, trigger.studyId, startedAt, null)
        return true
    }

    override suspend fun recordActivation(activation: TriggerActivation): Boolean {
        activations += activation
        return true
    }

    override suspend fun getActivationsForStudy(studyId: UUID): List<TriggerActivation> =
        activations.filter { it.studyId == studyId }

    override suspend fun getActivationsForTrigger(triggerId: UUID): List<TriggerActivation> =
        activations.filter { it.triggerId == triggerId }

    override suspend fun getAllScheduled(): List<ScheduledTrigger> =
        triggers.values.filterIsInstance<ScheduledTrigger>()

    override suspend fun getLatestActivationForTrigger(triggerId: UUID): TriggerActivation? =
        activations.filter { it.triggerId == triggerId }.maxByOrNull { it.firedAt }

    override suspend fun addActivation(activation: TriggerActivation) {
        activations += activation
    }

    override suspend fun endTrigger(triggerId: UUID): Boolean {
        return true
    }
}
