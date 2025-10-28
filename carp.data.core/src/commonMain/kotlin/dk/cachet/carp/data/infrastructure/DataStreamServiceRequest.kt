package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Serializable application service requests to [DataStreamServiceRequest] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class DataStreamServiceRequest<out TReturn> : ApplicationServiceRequest<DataStreamService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = DataStreamService.API_VERSION

    object Serializer : KSerializer<DataStreamServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class OpenDataStreams( val configuration: DataStreamsConfiguration ) : DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
    }

    @Serializable
    data class AppendToDataStreams(
        val studyDeploymentId: UUID,
        @Serializable( DataStreamBatchSerializer::class )
        val batch: DataStreamBatch
    ) : DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
    }

    @Serializable
    data class GetDataStream(
        val dataStream: DataStreamId,
        val fromSequenceId: Long,
        val toSequenceIdInclusive: Long? = null
    ) : DataStreamServiceRequest<DataStreamBatch>()
    {
        override fun getResponseSerializer() = DataStreamBatchSerializer
    }

    @Serializable
    data class GetBatchForStudyDeployments(
        val studyDeploymentIds: Set<UUID>,
        val deviceRoleNames: Set<String>? = null,
        val dataTypes: Set<DataType>? = null,
        val from: Instant? = null,
        val to: Instant? = null
    ) : DataStreamServiceRequest<DataStreamBatch>()
    {
        override fun getResponseSerializer() = DataStreamBatchSerializer
    }

    @Serializable
    data class CloseDataStreams( val studyDeploymentIds: Set<UUID> ) : DataStreamServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
    }

    @Serializable
    data class RemoveDataStreams( val studyDeploymentIds: Set<UUID> ) : DataStreamServiceRequest<Set<UUID>>()
    {
        override fun getResponseSerializer() = serializer<Set<UUID>>()
    }
}
