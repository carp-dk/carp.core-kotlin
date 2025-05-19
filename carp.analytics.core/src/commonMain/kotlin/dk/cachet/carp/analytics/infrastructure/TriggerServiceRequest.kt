package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.TriggerService
import dk.cachet.carp.analytics.domain.trigger.Trigger
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport

/**
 * Serializable application service requests to [TriggerService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
sealed class TriggerServiceRequest<out TReturn> :
    ApplicationServiceRequest<TriggerService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = TriggerService.API_VERSION

    object Serializer : KSerializer<TriggerServiceRequest<*>> by ignoreTypeParameters(::serializer)

    /**
     * Register a new trigger for a workflow.
     */
    @Serializable
    data class CreateTrigger(val trigger: Trigger) : TriggerServiceRequest<Trigger>() {
        override fun getResponseSerializer() =  serializer<Trigger>()  }

    /**
     * Update an existing trigger configuration.
     */
    @Serializable
    data class UpdateTrigger(val trigger: Trigger) : TriggerServiceRequest<Trigger>(){
        override fun getResponseSerializer() =  serializer<Trigger>()  }

    /**
     * Remove a registered trigger.
     */
    @Serializable
    data class DeleteTrigger(val triggerId: UUID) : TriggerServiceRequest<Boolean>(){
        override fun getResponseSerializer() =  serializer<Boolean>()  }

    /**
     * Retrieve a specific trigger by ID.
     */
    @Serializable
    data class GetTrigger(val triggerId: UUID) : TriggerServiceRequest<Trigger?>(){
        override fun getResponseSerializer() =  serializer<Trigger?>()  }

    /**
     * List all registered triggers for a given study.
     */
    @Serializable
    data class ListTriggers(val studyId: UUID) : TriggerServiceRequest<List<Trigger>>(){
        override fun getResponseSerializer() =  serializer<List<Trigger>>()  }

    /**
     * Manually initiate a trigger, such as for testing or manual override.
     * If no manual trigger exists, one may be automatically created and logged.
     */
    @Serializable
    data class StartTrigger(val triggerId: UUID, val at: Instant? = null) : TriggerServiceRequest<Boolean>() {
        override fun getResponseSerializer() =  serializer<Boolean>()  }

    /**
     * Conclude a previously started trigger (e.g. long-running or scheduled trigger).
     */
    @Serializable
    data class EndTrigger(val triggerId: UUID, val at: Instant? = null) : TriggerServiceRequest<Boolean>() {
        override fun getResponseSerializer() =  serializer<Boolean>()  }
}
