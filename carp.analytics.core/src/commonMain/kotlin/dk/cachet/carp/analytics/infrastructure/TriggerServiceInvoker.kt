package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.TriggerService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker

/**
 * Invokes [TriggerService] methods based on [TriggerServiceRequest]s.
 */
object TriggerServiceInvoker : ApplicationServiceInvoker<TriggerService, TriggerServiceRequest<*>>
{
    override suspend fun TriggerServiceRequest<*>.invoke(service: TriggerService): Any? =
        when (this)
        {
            is TriggerServiceRequest.CreateTrigger ->
                service.createTrigger(trigger)

            is TriggerServiceRequest.UpdateTrigger ->
                service.updateTrigger(trigger)

            is TriggerServiceRequest.DeleteTrigger ->
                service.deleteTrigger(triggerId)

            is TriggerServiceRequest.GetTrigger ->
                service.getTrigger(triggerId)

            is TriggerServiceRequest.ListTriggers ->
                service.listTriggers(studyId)

            is TriggerServiceRequest.StartTrigger ->
                service.startTrigger(triggerId, at ?: kotlinx.datetime.Clock.System.now())

            is TriggerServiceRequest.EndTrigger ->
                service.endTrigger(triggerId)

            is TriggerServiceRequest.RecordActivation ->
                service.recordActivation(activation)

            is TriggerServiceRequest.GetActivationsForTrigger ->
                service.getActivationsForTrigger(triggerId)

            is TriggerServiceRequest.ListByWorkflow ->
                service.listByWorkflow(studyId, workflowId)
        }
}
