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
     * Retrieve data across multiple study deployments, optionally filtered by device role names,
     * data types, and time range.
     *
     * The response is a canonical [DataStreamBatch]: for each [DataStreamId], sequences are
     * ordered by start time and non-overlapping (contract preserved). No derived/secondary
     * indexing is applied in this API; analytics-specific projections are out of scope here.
     *
     * Time range semantics: if [from] or [to] are specified, sequences are clipped to the
     * half-open interval [from, to) (inclusive start, exclusive end).
     *
     * @param studyDeploymentIds Study deployments to query. Must not be empty.
     * @param deviceRoleNames Optional device role name filter (e.g., "phone"). If null or empty, all are included.
     * @param dataTypes Optional data type filter. If null or empty, all are included.
     * @param from Optional absolute start time (inclusive). If null, no lower bound.
     * @param to Optional absolute end time (exclusive). If null, no upper bound.
     * @return A [DataStreamBatch] containing matching data sequences, preserving per-stream invariants.
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
