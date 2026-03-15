package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [MutableDataStreamBatch].
 */
class MutableDataStreamBatchTest
{
    private val stubDataPoint = StubDataPoint()
    private val stubDataTimeSpan = StubDataTimeSpan()

    @Test
    fun appendSequence_succeeds_with_no_prior_sequences()
    {
        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, stubDataPoint )

        batch.appendSequence( sequence )

        assertEquals( sequence.toList(), batch.getDataStreamPoints( sequence.dataStream ).toList() )
    }

    @Test
    fun appendSequence_succeeds_with_prior_sequence_with_gap()
    {
        val batch = MutableDataStreamBatch()
        val stubSequence = createStubSequence( 0, stubDataPoint )
        batch.appendSequence( stubSequence )

        val gapSequence = createStubSequence( 2, stubDataPoint )
        batch.appendSequence( gapSequence )

        assertEquals( 2, batch.sequences.count() )
        assertEquals( 2, batch.getDataStreamPoints( stubSequence.dataStream ).toList().count() )
    }

    @Test
    fun appendSequence_succeeds_with_new_triggerIds()
    {
        val batch = MutableDataStreamBatch()

        val dataStream = dataStreamId<StubDataPoint>( UUID.randomUUID(), "Device" )
        val measurement = measurement( StubDataPoint(), 0 )
        val triggerId1Sequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            0,
            listOf( 1 ),
            stubSyncPoint
        )
        triggerId1Sequence.appendMeasurements( measurement )
        val triggerId2Sequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            1,
            listOf( 2 ),
            stubSyncPoint
        )
        triggerId2Sequence.appendMeasurements( measurement )
        batch.appendSequence( triggerId1Sequence )
        batch.appendSequence( triggerId2Sequence )

        assertEquals( 2, batch.sequences.count() ) // Due to the different triggerIds, the sequence is not merged.
        assertEquals( 2, batch.getDataStreamPoints( dataStream ).toList().count() )
    }

    @Test
    fun appendSequence_succeeds_with_new_syncpoint()
    {
        val batch = MutableDataStreamBatch()
        val dataStream = dataStreamId<StubDataPoint>( UUID.randomUUID(), "Device" )
        val firstSequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            0,
            stubTriggerIds,
            SyncPoint.UnixEpoch
        )
        firstSequence.appendMeasurements( measurement( StubDataPoint(), 0 ) )
        batch.appendSequence( firstSequence )

        val newSequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            1,
            stubTriggerIds,
            stubSyncPoint
        )
        newSequence.appendMeasurements( measurement( StubDataPoint(), 0 ) )
        batch.appendSequence( newSequence )

        assertEquals( 2, batch.sequences.count() ) // Due to the different sync point, the sequence is not merged.
        assertEquals( 2, batch.getDataStreamPoints( dataStream ).toList().count() )
    }

    @Test
    fun appendSequence_merges_sequence_when_there_is_no_sequence_gap()
    {
        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, stubDataPoint ) )

        val noGapSequence = createStubSequence( 1, stubDataPoint )
        batch.appendSequence( noGapSequence )

        val singleSequence = batch.sequences.singleOrNull()
        assertNotNull( singleSequence )
        assertEquals( 0L..1, singleSequence.range )
    }

    @Test
    fun appendSequence_succeeds_for_differing_data_streams()
    {
        val batch = MutableDataStreamBatch()

        batch.appendSequence( createStubSequence( 0, stubDataPoint ) )
        batch.appendSequence( createStubSequence( 0, stubDataTimeSpan ) )

        assertEquals( 2, batch.sequences.count() )
    }

    @Test
    fun appendSequence_fails_for_overlapping_sequence_range()
    {
        val batch = MutableDataStreamBatch()
        batch.appendSequence( createStubSequence( 0, stubDataPoint, stubDataPoint ) )

        val overlappingSequence = createStubSequence( 1, stubDataPoint )
        assertFailsWith<IllegalArgumentException>
        {
            batch.appendSequence( overlappingSequence )
        }
    }

    @Test
    fun appendSequence_fails_for_older_sync_point()
    {
        val batch = MutableDataStreamBatch()
        val dataStream = dataStreamId<StubDataPoint>( UUID.randomUUID(), "Device" )
        val now = Clock.System.now()
        val firstSequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            0,
            stubTriggerIds,
            SyncPoint( now, now.toEpochMicroseconds() )
        )
        firstSequence.appendMeasurements( measurement( StubDataPoint(), 0 ) )
        batch.appendSequence( firstSequence )

        val newSequence = MutableDataStreamSequence<StubDataPoint>(
            dataStream,
            1,
            stubTriggerIds,
            SyncPoint.UnixEpoch
        )
        newSequence.appendMeasurements( measurement( StubDataPoint(), 0 ) )
        assertFailsWith<IllegalArgumentException> { batch.appendSequence( newSequence ) }
    }

    @Test
    fun appendBatch_succeeds()
    {
        val stubDataTimeSpanSequence = createStubSequence( 0, stubDataTimeSpan )
        val stubDataPointSequence = createStubSequence( 0, stubDataPoint )
        val batch = MutableDataStreamBatch().apply {
            appendSequence( stubDataTimeSpanSequence )
            appendSequence( stubDataPointSequence )
        }

        val appendBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 1, stubDataTimeSpan ) )
            appendSequence( createStubSequence( 1, stubDataPoint ) )
        }
        batch.appendBatch( appendBatch )

        assertEquals( 2, batch.getDataStreamPoints( stubDataTimeSpanSequence.dataStream ).count() )
        assertEquals( 2, batch.getDataStreamPoints( stubDataPointSequence.dataStream ).count() )
    }

    @Test
    fun appendBatch_fails_for_overlapping_sequence_range()
    {
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, stubDataPoint ) )
        }

        val appendBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, stubDataTimeSpan ) )
            appendSequence( createStubSequence( 0, stubDataPoint ) ) // Overlaps.
            appendSequence( createStubSequence( 2, stubDataPoint ) )
        }

        assertFailsWith<IllegalArgumentException> { batch.appendBatch( appendBatch ) }
        assertEquals( 1, batch.sequences.count() )
    }
}
