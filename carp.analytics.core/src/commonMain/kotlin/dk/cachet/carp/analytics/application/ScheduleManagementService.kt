package dk.cachet.carp.analytics.application

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

interface ScheduleManagementService : ApplicationService<ScheduleManagementService, ScheduleManagementService.Event> {

    companion object { val API_VERSION = ApiVersion(1, 0) }

    @Serializable
    sealed class Event : IntegrationEvent<ScheduleManagementService> {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }

    /**
     * Evaluate all scheduled triggers and execute any that are due.
     * Can be called manually (via API) or periodically by a scheduler.
     */
    suspend fun evaluateDueTriggers(now: Instant = kotlinx.datetime.Clock.System.now()) : Boolean
}
