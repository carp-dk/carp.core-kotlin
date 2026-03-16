package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlin.test.*


/**
 * Tests for implementations of [DataStreamSequence].
 */
interface DataStreamSequenceTest
{
    fun <T : Data> createStubDataStreamSequence(
        firstSequenceId: Long = 0,
        measurements: List<Measurement<T>>,
        triggerIds: List<Int> = listOf( 1 ),
        syncPoint: SyncPoint = stubSyncPoint,
        dataStream: DataStreamId = stubDataPointStream
    ): DataStreamSequence<T>


    @Test
    fun range_for_empty_sequence()
    {
        val empty = createStubDataStreamSequence<StubDataPoint>( 0, emptyList() )

        assertEquals( LongRange.EMPTY, empty.range )
    }

    @Test
    fun range_for_sequence_with_one_item()
    {
        val measurement = measurement( StubDataPoint(), 0 )
        val oneItem = createStubDataStreamSequence( 10, listOf( measurement ) )

        assertEquals( 10L until 11L, oneItem.range )
    }

    @Test
    fun isImmediatelyFollowedBy_is_false_for_non_matching_data_stream()
    {
        val measurement = measurement( StubDataPoint(), 0 )
        val sequence = createStubDataStreamSequence( 0, listOf( measurement ), triggerIds = listOf( 0 ) )
        val wrongTrigger = createStubDataStreamSequence( 1, listOf( measurement ), triggerIds = listOf( 1 ) )

        assertFalse( sequence.isImmediatelyFollowedBy( wrongTrigger ) )
    }

    @Test
    fun isImmediatelyFollowedBy_is_false_when_there_is_a_gap()
    {
        val measurement = measurement( StubDataPoint(), 0 )
        val sequence = createStubDataStreamSequence(0, listOf( measurement ) )
        val doesNotFollow = createStubDataStreamSequence( 2, listOf( measurement ) )

        assertFalse( sequence.isImmediatelyFollowedBy( doesNotFollow ) )
    }

    @Test
    fun isImmediatelyFollowedBy_is_true_for_empty_sequence()
    {
        val measurement = measurement( StubDataPoint(), 0 )
        val emptySequence = createStubDataStreamSequence<StubDataPoint>( 0, emptyList() )
        val firstItem = createStubDataStreamSequence( 0, listOf( measurement ) )

        assertTrue( emptySequence.isImmediatelyFollowedBy( firstItem ) )
    }

    @Test
    fun getDataStreamPoints_succeeds()
    {
        val deploymentId = UUID.randomUUID()
        val device = "Device"

        val measurement1 = measurement( StubDataPoint(), 0 )
        val measurement2 = measurement( StubDataPoint(), 1 )

        val triggerIds = listOf( 1 )

        val sequence = createStubDataStreamSequence(
            0,
            listOf( measurement1, measurement2 ),
            dataStream = dataStreamId<StubDataPoint>( deploymentId, device ),
        )

        val expectedPoints = listOf(
            DataStreamPoint( 0, deploymentId, device, measurement1, triggerIds, stubSyncPoint ),
            DataStreamPoint( 1, deploymentId, device, measurement2, triggerIds, stubSyncPoint )
        )
        assertEquals( expectedPoints, sequence.toList() )
    }
}


/**
 * Tests for [MutableDataStreamSequence].
 */
class MutableDataStreamSequenceTest : DataStreamSequenceTest
{
    override fun <T : Data> createStubDataStreamSequence(
        firstSequenceId: Long,
        measurements: List<Measurement<T>>,
        triggerIds: List<Int>,
        syncPoint: SyncPoint,
        dataStream: DataStreamId
    ): DataStreamSequence<T>
    {
        val sequence = MutableDataStreamSequence<T>( dataStream, firstSequenceId, triggerIds, syncPoint )
        sequence.appendMeasurements( measurements )

        return sequence
    }


    @Test
    fun initialization_with_negative_firstSequenceId_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            MutableDataStreamSequence<StubDataPoint>( stubDataPointStream, -1, listOf( 1 ), stubSyncPoint )
        }
    }

    @Test
    fun initialization_with_empty_triggerIds_fails()
    {
        assertFailsWith<IllegalArgumentException>
        {
            MutableDataStreamSequence<StubDataPoint>( stubDataPointStream, 0, emptyList(), stubSyncPoint )
        }
    }

    @Test
    fun appendMeasurements_succeeds()
    {
        val sequence = MutableDataStreamSequence<StubDataPoint>( stubDataPointStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements(
            measurement( StubDataPoint(), 0 ),
            measurement( StubDataPoint(), 10 )
        )

        assertEquals( 0L..1, sequence.range )
    }

    @Test
    fun appendMeasurements_fails_for_incorrect_data_type()
    {
        val sequence = MutableDataStreamSequence<Data>( stubDataPointStream, 0, listOf( 1 ), stubSyncPoint )

        assertFailsWith<IllegalArgumentException> { sequence.appendMeasurements( measurement( StubDataTimeSpan(), 0 ) ) }
    }

    @Test
    fun appendMeasurement_fails_when_list_contains_incorrect_data_type()
    {
        val sequence = MutableDataStreamSequence<Data>( stubDataPointStream, 0, listOf( 1 ), stubSyncPoint )

        assertFailsWith<IllegalArgumentException>
        {
            sequence.appendMeasurements(
                measurement( StubDataPoint(), 0 ),
                measurement( StubDataTimeSpan(), 0 ) // Incorrect for `stubDataPointStream`.
            )
        }
    }

    @Test
    fun appendSequence_succeeds()
    {
        val measurement = measurement( StubDataPoint(), 0 )

        val sequence = MutableDataStreamSequence<StubDataPoint>( stubDataPointStream, 0, listOf( 1 ), stubSyncPoint )
        sequence.appendMeasurements( measurement )

        val toAppend = MutableDataStreamSequence<StubDataPoint>( stubDataPointStream, 1, listOf( 1 ), stubSyncPoint )
        toAppend.appendMeasurements( measurement )

        sequence.appendSequence( toAppend )
        assertEquals( 0L..1, sequence.range )
    }
}
