package dk.cachet.carp.analytics.infrastructure.scheduleManager

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.application.ScheduleManagementService
import dk.cachet.carp.analytics.application.TriggerRepository
import kotlinx.datetime.Instant

/**
 * A simple reference implementation of [ScheduleManagementService] which immediately executes workflows
 * for any [dk.cachet.carp.analytics.domain.trigger.ScheduledTrigger]s whose [dk.cachet.carp.analytics.domain.trigger.CronExpression]
 * matches the evaluation time.
 */
class ReferenceScheduleManagementService(
    private val triggerRepo: TriggerRepository,
    private val executionService: ExecutionService
) : ScheduleManagementService {

    /**
     * Evaluates all scheduled triggers and executes their workflows if the current time matches their cron schedule.
     *
     * @param now The current time to evaluate against the triggers' schedules.
     * @return `true` if any triggers were activated, `false` otherwise.
     * Note: consider changing the return type to `List<TriggerActivation>`
     */
    override suspend fun evaluateDueTriggers(now: Instant): Boolean {
        val scheduledTriggers = triggerRepo.getAllScheduled()
        var anyTriggered = false

        for (trigger in scheduledTriggers) {
            val lastFired = triggerRepo.getLatestActivationForTrigger(trigger.id)?.firedAt

            if (trigger.cron.shouldTriggerAt(now, lastFired)) {

                val executionState = executionService.executeWorkflow(
                    trigger.studyId,
                    trigger.workflowId
                )

                val activation = trigger.activate(now, executionState.executionId)
                triggerRepo.addActivation(activation)

                anyTriggered = true
            }
        }

        return anyTriggered
    }
}
