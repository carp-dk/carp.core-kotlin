package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ScheduleManagementService
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.js.JsExport

@Serializable
@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
sealed class ScheduleManagementServiceRequest<out TReturn> :
    ApplicationServiceRequest<ScheduleManagementService, TReturn>()
{

    @Required
    override val apiVersion = ScheduleManagementService.API_VERSION

    object Serializer : KSerializer<ScheduleManagementServiceRequest<*>> by ignoreTypeParameters(::serializer)

    @Serializable
    data class EvaluateDueTriggers(
        val now: Instant = kotlinx.datetime.Clock.System.now()
    ) : ScheduleManagementServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
    }
}
