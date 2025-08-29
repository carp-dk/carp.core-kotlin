package dk.cachet.carp.data.application


import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Store and retrieve [DataStreamPoint]s for study deployments.
 */
interface DataStreamService : ApplicationService<DataStreamService, DataStreamService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 1 ) }

    @Serializable
    sealed class Event : IntegrationEvent<DataStreamService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Start accepting data for a study deployment for data streams configured in [configuration].
     *
     * @throws IllegalStateException when data streams for the specified study deployment have already been configured.
     */
    suspend fun openDataStreams( configuration: DataStreamsConfiguration )

    /**
     * Append a [batch] of data point sequences to corresponding data streams in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     *  - the `studyDeploymentId` of one or more sequences in [batch] does not match [studyDeploymentId]
     *  - the start of one or more of the sequences contained in [batch]
     *  precede the end of a previously appended sequence to the same data stream
     *  - [batch] contains a sequence with [DataStreamId] which wasn't configured for [studyDeploymentId]
     * @throws IllegalStateException when data streams for [studyDeploymentId] have been closed.
     */
    suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch )

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     *
     * @throws IllegalArgumentException if:
     *  - [dataStream] has never been opened
     *  - [fromSequenceId] is negative or [toSequenceIdInclusive] is smaller than [fromSequenceId]
     */
    suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long? = null
    ): DataStreamBatch

    /**
     * Fetch a batch of collected data across multiple study deployments.
     *
     * Clients do not need individual DataStreamIds. The service gathers data from all
     * streams belonging to [studyDeploymentIds], optionally filtering by [deviceRoleNames]
     * and [dataTypes] within the time window [from]..[to].
     *
     * Returns an immutable batch optimized for analytics with data organized by device role name
     * and data type. Timestamps are normalized via SyncPoints (epoch microseconds).
     *
     * @param studyDeploymentIds The study deployments to gather data from
     * @param deviceRoleNames Optional filter for specific device role names
     * @param dataTypes Optional filter for specific data types
     * @param from Optional start time for the data window
     * @param to Optional end time for the data window
     * @return An immutable batch containing the filtered data organized for analytics
     */
    suspend fun getBatchForStudyDeployments(
        studyDeploymentIds: Set<UUID>,
        deviceRoleNames: Set<String>? = null,
        dataTypes: Set<DataType>? = null,
        from: Instant? = null,
        to: Instant? = null
    ): DataStreamBatch


    /**
     * Stop accepting incoming data for all data streams for each of the [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when no data streams were ever opened for any of the [studyDeploymentIds].
     */
    suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> )

    /**
     * Close data streams and remove all data for each of the [studyDeploymentIds].
     *
     * @return The IDs of the study deployments for which data streams were configured.
     * IDs for which no study deployment exists are ignored.
     */
    suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ): Set<UUID>
}
