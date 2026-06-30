package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.intersect
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamStatus
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence


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
    private val lastSequenceIdByDataStream: MutableMap<DataStreamId, Long?> = mutableMapOf()


    /**
     * Start accepting data for a study deployment for data streams configured in [configuration].
     *
     * @throws IllegalStateException when data streams for the specified study deployment have already been configured.
     */
    override suspend fun openDataStreams( configuration: DataStreamsConfiguration )
    {
        configuredDataStreams.tryAddIfKeyIsNew( configuration )
        configuration.expectedDataStreamIds.forEach { dataStreamId ->
            lastSequenceIdByDataStream[ dataStreamId ] = null
        }
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

        // Update last sequence ID for all streams with new data.
        batch.sequences
            .groupBy { it.dataStream }
            .forEach { (dataStreamId, dataStreamSequences) ->
                lastSequenceIdByDataStream[ dataStreamId ] = dataStreamSequences.last().range.last
            }
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
     * Retrieve status for each configured data stream in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when no data streams were ever opened for [studyDeploymentId].
     */
    override suspend fun getDataStreamsStatus( studyDeploymentId: UUID ): List<DataStreamStatus>
    {
        val configuration: DataStreamsConfiguration? = configuredDataStreams[ studyDeploymentId ]
        requireNotNull( configuration ) { "No data streams configured for this study deployment." }

        val isOpen = studyDeploymentId !in stoppedStudyDeploymentIds
        return configuration.expectedDataStreamIds.map { dataStream ->
            val lastSequenceId: Long? = lastSequenceIdByDataStream[ dataStream ]
            DataStreamStatus( dataStream, lastSequenceId, isOpen )
        }
    }

    /**
     * Stop accepting incoming data for all data streams for each of the [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when no data streams were ever opened for any of the [studyDeploymentIds].
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

        val removedStudyDeploymentIds = studyDeploymentIds.mapNotNull { toRemove ->
            if ( configuredDataStreams.removeKey( toRemove ) ) toRemove
            else null
        }.toSet()

        lastSequenceIdByDataStream.keys.removeAll { dataStreamId ->
            dataStreamId.studyDeploymentId in removedStudyDeploymentIds
        }

        return removedStudyDeploymentIds
    }
}
