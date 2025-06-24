package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.data.application.CollectedDataSet
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Serializable application service requests to [StudyDataService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
sealed class StudyDataServiceRequest<out TReturn> : ApplicationServiceRequest<StudyDataService, TReturn>()
{

    @Required
    override val apiVersion: ApiVersion = StudyDataService.API_VERSION

    object Serializer : kotlinx.serialization.KSerializer<StudyDataServiceRequest<*>> by ignoreTypeParameters(::serializer)

    /**
     * Query collected data for a study with optional subject, field, and time filters.
     */
    @Serializable
    data class GetCollectedData(
        val studyId: UUID,
        val studyDeploymentIds: Set<UUID>? = null,
        val deviceRoleNames: Set<String>? = null,
        val fields: Set<String>? = null,
        val from: Instant? = null,
        val to: Instant? = null,
        val offsetDays: Int? = null
    ) : StudyDataServiceRequest<CollectedDataSet>()
    {
        override fun getResponseSerializer() = CollectedDataSet.serializer()
    }
}
