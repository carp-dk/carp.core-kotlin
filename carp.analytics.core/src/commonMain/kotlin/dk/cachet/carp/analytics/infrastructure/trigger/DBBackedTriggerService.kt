package dk.cachet.carp.analytics.infrastructure.trigger

import dk.cachet.carp.analytics.application.TriggerRepository
import dk.cachet.carp.analytics.application.TriggerService
import dk.cachet.carp.analytics.domain.trigger.Trigger
import dk.cachet.carp.analytics.domain.trigger.TriggerActivation
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

class DBBackedTriggerService(
    private val repo: TriggerRepository
) : TriggerService {

    override suspend fun createTrigger(trigger: Trigger): Trigger =
        repo.create(trigger)

    override suspend fun updateTrigger(trigger: Trigger): Trigger =
        repo.update(trigger)

    override suspend fun deleteTrigger(triggerId: UUID): Boolean =
        repo.delete(triggerId)

    override suspend fun getTrigger(triggerId: UUID): Trigger? =
        repo.get(triggerId)

    override suspend fun listTriggers(studyId: UUID): List<Trigger> =
        repo.list(studyId)

    override suspend fun listByWorkflow(studyId: UUID, workflowId: UUID): List<Trigger> =
        repo.listByWorkflow(studyId, workflowId)

    override suspend fun startTrigger(triggerId: UUID, at: Instant): Boolean =
        repo.startTrigger(triggerId, at)

    override suspend fun endTrigger(triggerId: UUID): Boolean =
        repo.endTrigger(triggerId)

    override suspend fun recordActivation(activation: TriggerActivation): Boolean =
        repo.recordActivation(activation)

    override suspend fun getActivationsForTrigger(triggerId: UUID): List<TriggerActivation> =
        repo.getActivationsForTrigger(triggerId)
}
