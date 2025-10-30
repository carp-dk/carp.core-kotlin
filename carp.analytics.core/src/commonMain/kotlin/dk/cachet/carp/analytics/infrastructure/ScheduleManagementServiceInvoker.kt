package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ScheduleManagementService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker

/**
 * Invokes [ScheduleManagementService] methods based on [ScheduleManagementServiceRequest]s.
 */
object ScheduleManagementServiceInvoker :
    ApplicationServiceInvoker<ScheduleManagementService, ScheduleManagementServiceRequest<*>>
{

    override suspend fun ScheduleManagementServiceRequest<*>.invoke( service: ScheduleManagementService ): Any? =
        when (this)
        {
            is ScheduleManagementServiceRequest.EvaluateDueTriggers ->
                service.evaluateDueTriggers(now)
        }
}
