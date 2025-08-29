package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import kotlinx.datetime.Instant


class DataStreamServiceDecorator(
    service: DataStreamService,
    requestDecorator: (Command<DataStreamServiceRequest<*>>) -> Command<DataStreamServiceRequest<*>>
) : ApplicationServiceDecorator<DataStreamService, DataStreamServiceRequest<*>>(
        service,
        DataStreamServiceInvoker,
        requestDecorator
    ),
    DataStreamService
{
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration ) =
        invoke( DataStreamServiceRequest.OpenDataStreams( configuration ) )

    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch ) =
        invoke( DataStreamServiceRequest.AppendToDataStreams( studyDeploymentId, batch ) )

    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ) = invoke( DataStreamServiceRequest.GetDataStream( dataStream, fromSequenceId, toSequenceIdInclusive ) )

    override suspend fun getBatchForStudyDeployments(
        studyDeploymentIds: Set<UUID>,
        deviceRoleNames: Set<String>?,
        dataTypes: Set<DataType>?,
        from: Instant?,
        to: Instant?
    ) = invoke(
        DataStreamServiceRequest.GetBatchForStudyDeployments(
        studyDeploymentIds,
        deviceRoleNames,
        dataTypes,
        from,
        to
    )
    )

    override suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> ) =
        invoke( DataStreamServiceRequest.CloseDataStreams( studyDeploymentIds ) )

    override suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ) =
        invoke( DataStreamServiceRequest.RemoveDataStreams( studyDeploymentIds ) )
}


object DataStreamServiceInvoker : ApplicationServiceInvoker<DataStreamService, DataStreamServiceRequest<*>>
{
    override suspend fun DataStreamServiceRequest<*>.invoke( service: DataStreamService ): Any =
        when ( this )
        {
            is DataStreamServiceRequest.OpenDataStreams -> service.openDataStreams( configuration )
            is DataStreamServiceRequest.AppendToDataStreams -> service.appendToDataStreams( studyDeploymentId, batch )
            is DataStreamServiceRequest.GetDataStream ->
                service.getDataStream( dataStream, fromSequenceId, toSequenceIdInclusive )
            is DataStreamServiceRequest.CloseDataStreams -> service.closeDataStreams( studyDeploymentIds )
            is DataStreamServiceRequest.RemoveDataStreams -> service.removeDataStreams( studyDeploymentIds )
            is DataStreamServiceRequest.GetBatchForStudyDeployments -> service.getBatchForStudyDeployments(
                studyDeploymentIds,
                deviceRoleNames,
                dataTypes,
                from,
                to
            )
        }
}
