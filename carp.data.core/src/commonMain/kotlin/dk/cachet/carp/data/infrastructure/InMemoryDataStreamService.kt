package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.intersect
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.applyToTimestamp
import kotlinx.datetime.Instant


/**
 * A [DataStreamService] which holds data points in memory as long as the instance is held in memory.
 */
class InMemoryDataStreamService : DataStreamService
{
    private val configuredDataStreams: ExtractUniqueKeyMap<UUID, DataStreamsConfiguration> =
        ExtractUniqueKeyMap( { configuration -> configuration.studyDeploymentId } )
        {
                studyDeploymentId ->
            IllegalStateException(
                "Data streams for deployment with \"$studyDeploymentId\" have already been configured."
            )
        }
    private val stoppedStudyDeploymentIds: MutableSet<UUID> = mutableSetOf()
    private val dataStreams: MutableDataStreamBatch = MutableDataStreamBatch()

    companion object
    {
        private const val MICROSECONDS_TO_MILLISECONDS = 1000L
    }

    /**
     * Start accepting data for a study deployment for data streams configured in [configuration].
     *
     * @throws IllegalStateException when data streams for the specified study deployment have already been configured.
     */
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration )
    {
        configuredDataStreams.tryAddIfKeyIsNew( configuration )
    }

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
    override suspend fun appendToDataStreams( studyDeploymentId: UUID, batch: DataStreamBatch )
    {
        require( batch.sequences.all { it.dataStream.studyDeploymentId == studyDeploymentId } )
        { "The study deployment ID of one or more sequences in `batch` doesn't match `studyDeploymentId`." }

        val configuration = configuredDataStreams[ studyDeploymentId ]
        requireNotNull( configuration ) { "No data streams configured for this study deployment." }
        require( batch.sequences.all { it.dataStream in configuration.expectedDataStreamIds } )
        { "The batch contains a sequence with a data stream which wasn't configured for this study deployment." }

        check( studyDeploymentId !in stoppedStudyDeploymentIds )
        { "Data streams for this study deployment have been closed." }

        dataStreams.appendBatch( batch )
    }

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
    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?
    ): DataStreamBatch
    {
        val configuration = configuredDataStreams[ dataStream.studyDeploymentId ]
        requireNotNull( configuration ) { "No data streams configured for this study deployment." }
        require( dataStream in configuration.expectedDataStreamIds )
        { "The batch contains a sequence with a data stream which wasn't configured for this study deployment." }
        require( fromSequenceId >= 0 && (toSequenceIdInclusive == null || toSequenceIdInclusive >= fromSequenceId) )
        { "The starting sequence ID is negative or the end sequence ID is smaller than the starting ID." }

        return dataStreams.sequences
            .filter { it.dataStream == dataStream }
            .mapNotNull {
                val queryRange = fromSequenceId.rangeTo( toSequenceIdInclusive ?: Long.MAX_VALUE )
                val subRange = it.range.intersect( queryRange )

                if ( subRange.isEmpty() ) null
                else MutableDataStreamSequence<Data>( dataStream, subRange.first, it.triggerIds, it.syncPoint )
                    .apply {
                        val startOffset = subRange.first - it.range.first
                        val exclusiveEnd = startOffset + subRange.last - subRange.first + 1
                        check( startOffset <= Int.MAX_VALUE && exclusiveEnd <= Int.MAX_VALUE )
                        { "Exceeded capacity of measurements which can be held in memory." }
                        appendMeasurements( it.measurements.subList( startOffset.toInt(), exclusiveEnd.toInt() ) )
                    }
            }
            .fold( MutableDataStreamBatch() ) { batch, sequence ->
                batch.apply { appendSequence( sequence ) }
            }
    }

    /**
     * Retrieve collected data points for the specified study deployments, optionally filtered by device role names, data types, and time range.
     *
     * @param studyDeploymentIds The set of study deployment IDs to query. Must not be empty.
     * @param deviceRoleNames Optional set of device role names (e.g., "phone") to include. If null or empty, data for all device roles is returned.
     * @param dataTypes Optional set of [DataType]s to include. If null or empty, data for all data types is returned.
     * @param from Optional absolute start time for filtering (inclusive). If null, no lower bound is applied.
     * @param to Optional absolute end time for filtering (exclusive). If null, no upper bound is applied.
     *
     * @return A [DataStreamBatch] containing matching data points.
     */
    override suspend fun getBatchForStudyDeployments(
        studyDeploymentIds: Set<UUID>,
        deviceRoleNames: Set<String>?,
        dataTypes: Set<DataType>?,
        from: Instant?,
        to: Instant?
    ): DataStreamBatch
    {
        // 1) Apply basic filters and clip by time to contiguous chunks
        val prelim = dataStreams.sequences
            .filter { matchesBasicFilters(it, studyDeploymentIds, deviceRoleNames, dataTypes) }
            .flatMap { seq -> clipByTimeToChunks(seq, from, to).asSequence() } // 0..N sequences per original
            .toList()

        // 2) Group by data stream and sort sequences by start of range to ensure adherence to DataStream interface
        val byStream = prelim.groupBy { it.dataStream }
            .mapValues { (_, seqs) -> seqs.sortedBy { it.range.first } }

        // 3) Append per stream to preserve contract
        val batch = MutableDataStreamBatch()
        byStream.forEach { (_, seqs) ->
            seqs.forEach { batch.appendSequence(it) }
        }
        return batch
    }

    /**
     * Checks if a sequence matches the basic filters (deployment ID, device role, data type).
     */
    private fun matchesBasicFilters(
        sequence: DataStreamSequence<*>,
        studyDeploymentIds: Set<UUID>,
        deviceRoleNames: Set<String>?,
        dataTypes: Set<DataType>?
    ): Boolean
    {
        // Check deployment match first - most restrictive filter
        if (sequence.dataStream.studyDeploymentId !in studyDeploymentIds)
        {
            return false
        }

        // Then check device role
        if (!deviceRoleNames.isNullOrEmpty() &&
            sequence.dataStream.deviceRoleName !in deviceRoleNames
        )
        {
            return false
        }

        // Finally check data type
        if (!dataTypes.isNullOrEmpty() &&
            sequence.dataStream.dataType !in dataTypes
        )
        {
            return false
        }

        return true
    }

    /**
     * Returns 0...N clipped sequences for [sequence] that intersect [from, to).
     * Builds contiguous chunks by index where the time predicate holds.
     * Each chunk becomes its own sequence with an adjusted firstSequenceId.
     * This approach works even if time is not monotonic.
     */
    private fun clipByTimeToChunks(
        sequence: DataStreamSequence<*>,
        from: Instant?,
        to: Instant?
    ): List<DataStreamSequence<*>>
    {
        if (from == null && to == null) return listOf(sequence)

        val ms = sequence.measurements
        if (ms.isEmpty()) return emptyList()

        val chunks = mutableListOf<DataStreamSequence<*>>()
        var startIdx = -1

        fun keep( mIdx: Int ): Boolean =
            isWithinTimeRange(ms[mIdx], sequence, from, to)

        for (i in ms.indices)
        {
            val inRange = keep(i)
            if (inRange)
            {
                if (startIdx == -1) startIdx = i
            } else if (startIdx != -1)
            {
                // close chunk [startIdx, i)
                chunks += buildSlice(sequence, startIdx, i) // i is exclusive
                startIdx = -1
            }
        }
        if (startIdx != -1) chunks += buildSlice(sequence, startIdx, ms.size)

        return chunks
    }

    /**
     * Builds a slice of a sequence from [fromIdx] to [toIdxExclusive].
     */
    private fun buildSlice(
        sequence: DataStreamSequence<*>,
        fromIdx: Int,
        toIdxExclusive: Int
    ): DataStreamSequence<*>
    {
        val newFirst = sequence.firstSequenceId + fromIdx
        val slice = sequence.measurements.subList(fromIdx, toIdxExclusive)
        return MutableDataStreamSequence<Data>(
            sequence.dataStream,
            newFirst,
            sequence.triggerIds,
            sequence.syncPoint
        ).apply { appendMeasurements(slice) }
    }

    /**
     * Checks if a measurement falls within the specified time range [from, to).
     * Uses half-open interval to avoid double-counting on boundaries.
     *
     * @param from Inclusive start time
     * @param to Exclusive end time
     */
    private fun isWithinTimeRange(
        measurement: Measurement<*>,
        sequence: DataStreamSequence<*>,
        from: Instant?,
        to: Instant?
    ): Boolean
    {
        // Convert sensor timestamps to absolute time using the sync point
        val absoluteStartTime = Instant.fromEpochMilliseconds(
            sequence.syncPoint.applyToTimestamp(measurement.sensorStartTime) / MICROSECONDS_TO_MILLISECONDS
        )

        val absoluteEndTime = measurement.sensorEndTime?.let { endTime ->
            Instant.fromEpochMilliseconds(
                sequence.syncPoint.applyToTimestamp(endTime) / MICROSECONDS_TO_MILLISECONDS
            )
        } ?: absoluteStartTime

        // Accept a measurement if its [start, end] intersects [from, to)
        val afterFrom = from == null || absoluteEndTime >= from
        val beforeTo = to == null || absoluteStartTime < to

        return afterFrom && beforeTo
    }

    /**
     * Stop accepting data for the specified [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when one or more of the specified [studyDeploymentIds]
     * do not have configured data streams.
     */
    override suspend fun closeDataStreams( studyDeploymentIds: Set<UUID> )
    {
        require( studyDeploymentIds.all { configuredDataStreams[ it ] != null } )
        { "No data streams configured for this study deployment." }

        stoppedStudyDeploymentIds.addAll( studyDeploymentIds )
    }

    /**
     * Close data streams and remove all data for each of the [studyDeploymentIds].
     *
     * @return The IDs of the study deployments for which data streams were configured.
     * IDs for which no study deployment exists are ignored.
     */
    override suspend fun removeDataStreams( studyDeploymentIds: Set<UUID> ): Set<UUID>
    {
        stoppedStudyDeploymentIds.removeAll( studyDeploymentIds )

        return studyDeploymentIds.mapNotNull { toRemove ->
            if ( configuredDataStreams.removeKey( toRemove ) ) toRemove
            else null
        }.toSet()
    }
}
