package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTimeSpan
import dk.cachet.carp.data.infrastructure.dataStreamId
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests for implementations of [DataStreamService].
 */
interface DataStreamServiceTest
{
    /**
     * Create a [DataStreamService] to be used in the tests.
     */
    fun createService(): DataStreamService


    @Test
    fun openDataStreams_succeeds() = runTest {
        val service = createService()

        val dataStreamId = dataStreamId<StubDataPoint>( stubDeploymentId, "Some device" )
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( dataStreamId )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        service.openDataStreams( configuration )

        val retrievedDataStream = service.getDataStream( dataStreamId, 0 )
        assertTrue( retrievedDataStream.isEmpty() )
    }

    @Test
    fun openDataStreams_fails_when_data_streams_already_opened() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val expectedStream = DataStreamsConfiguration.ExpectedDataStream( "Some device", STUB_DATA_POINT_TYPE )
        val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
        assertFailsWith<IllegalStateException> { service.openDataStreams( configuration ) }
    }

    @Test
    fun appendToDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence(
            0,
            StubDataPoint(),
            StubDataPoint( sensorSpecificData = StubDataPoint( "sensor specific" ) )
        )
        batch.appendSequence( sequence )
        service.appendToDataStreams( stubDeploymentId, batch )

        val retrievedSequence = service.getDataStream( sequence.dataStream, 0 )
        assertEquals( batch, retrievedSequence )
    }

    @Test
    fun appendToDataStreams_fails_for_preceding_sequence() = runTest {
        val service = createServiceWithOpenStubDataPointStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint(), StubDataPoint() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        val appendBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 1, StubDataPoint() ) )
        }
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( stubDeploymentId, appendBatch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_nonmatching_studyDeploymentId() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint(), StubDataPoint() ) )
        }
        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( unknownDeploymentId, batch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_unexpected_data_stream() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val unexpectedBatch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataTimeSpan() ) ) // `StubDataTimeSpan` is unexpected.
        }
        assertFailsWith<IllegalArgumentException> { service.appendToDataStreams( stubDeploymentId, unexpectedBatch ) }
    }

    @Test
    fun appendToDataStreams_fails_for_closed_data_streams() = runTest {
        val service = createServiceWithOpenStubDataPointStream()
        service.closeDataStreams( setOf( stubDeploymentId ) )

        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint() ) )
        }
        assertFailsWith<IllegalStateException> { service.appendToDataStreams( stubDeploymentId, batch ) }
    }

    @Test
    fun getDataStream_fails_for_unopened_streams() = runTest {
        val service = createService()

        val unopenedStreamId = dataStreamId<StubDataPoint>( stubDeploymentId, stubSequenceDeviceRoleName )
        assertFailsWith<IllegalArgumentException> {
            service.getDataStream( unopenedStreamId, 0 )
        }
    }

    @Test
    fun getDataStream_fails_for_wrong_sequence_ids() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val batch = MutableDataStreamBatch()
        val sequence = createStubSequence( 0, StubDataPoint(), StubDataPoint() )
        batch.appendSequence( sequence )
        service.appendToDataStreams( stubDeploymentId, batch )

        assertFailsWith<IllegalArgumentException> { service.getDataStream( sequence.dataStream, -1, 10 ) }
        assertFailsWith<IllegalArgumentException> { service.getDataStream( sequence.dataStream, 1, 0 ) }
    }

    @Test
    fun closeDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        service.closeDataStreams( setOf( stubDeploymentId ) )
    }

    @Test
    fun closeDataStreams_fails_for_unopened_data_streams() = runTest {
        val service = createService()

        val unknownDeploymentId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException>
        {
            service.closeDataStreams( setOf( unknownDeploymentId ) )
        }
    }

    @Test
    fun removeDataStreams_succeeds() = runTest {
        val service = createServiceWithOpenStubDataPointStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint(), StubDataPoint() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        val removed = service.removeDataStreams( setOf( stubDeploymentId ) )
        assertEquals( setOf( stubDeploymentId ), removed )

        val dataStreamId = dataStreamId<StubDataPoint>( stubDeploymentId, stubSequenceDeviceRoleName )
        assertFailsWith<IllegalArgumentException> { service.getDataStream( dataStreamId, 0 ) }
    }

    @Test
    fun removeDataStream_ignores_unknown_ids() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val unknownDeploymentid = UUID.randomUUID()
        val removed = service.removeDataStreams( setOf( stubDeploymentId, unknownDeploymentid ) )

        assertEquals( setOf( stubDeploymentId ), removed )
    }

    @Test
    fun getBatchForStudyDeployments_succeeds() = runTest {
        val service = createServiceWithOpenStubDataPointStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint(), StubDataPoint() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        val collectedData = service.getBatchForStudyDeployments( setOf( stubDeploymentId ) )

        assertNotNull( collectedData )
        assertTrue( collectedData.sequences.toList().isNotEmpty() )
    }

    @Test
    fun getBatchForStudyDeployments_succeeds_with_empty_result() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val collectedData = service.getBatchForStudyDeployments( setOf( stubDeploymentId ) )

        assertNotNull( collectedData )
        assertTrue( collectedData.sequences.toList().isEmpty() )
    }

    @Test
    fun getBatchForStudyDeployments_succeeds_with_mixed_valid_invalid_deployment_ids() = runTest {
        val service = createServiceWithOpenStubDataPointStream()

        val unknownDeploymentId = UUID.randomUUID()
        val collectedData = service.getBatchForStudyDeployments( setOf( stubDeploymentId, unknownDeploymentId ) )

        assertNotNull( collectedData )
        // Should handle mixed valid/invalid deployment IDs gracefully
        assertTrue( collectedData.sequences.toList().isEmpty() ) // No data added yet
    }

    @Test
    fun getBatchForStudyDeployments_does_not_fail_when_all_deployment_ids_are_invalid() = runTest {
        val service = createService()

        val unknownDeploymentId = UUID.randomUUID()
        val batch = service.getBatchForStudyDeployments(setOf(stubDeploymentId, unknownDeploymentId))

        assertNotNull(batch)
    }

    @Test
    fun getBatchForStudyDeployments_succeeds_with_empty_deployment_set() = runTest {
        val service = createService()

        val collectedData = service.getBatchForStudyDeployments( emptySet() )

        assertNotNull( collectedData )
        assertTrue( collectedData.sequences.toList().isEmpty() )
    }

    @Test
    fun getBatchForStudyDeployments_with_filters_succeeds() = runTest {
        val service = createServiceWithOpenStubDataPointStream()
        val batch = MutableDataStreamBatch().apply {
            appendSequence( createStubSequence( 0, StubDataPoint() ) )
        }
        service.appendToDataStreams( stubDeploymentId, batch )

        // Test that optional parameters don't break the API
        val collectedData = service.getBatchForStudyDeployments(
            setOf( stubDeploymentId ),
            deviceRoleNames = setOf( stubSequenceDeviceRoleName ),
            dataTypes = setOf( STUB_DATA_POINT_TYPE ),
            from = now,
            to = now
        )

        assertNotNull( collectedData )
        // API should succeed regardless of filter results
    }

    /**
     * Create a data stream service and open [stubDataPointStream] for [stubDeploymentId].
     */
    private suspend fun createServiceWithOpenStubDataPointStream(): DataStreamService =
        createService().apply {
            // Device name corresponds to the one created by `createStubSequence`.
            val expectedStream = DataStreamsConfiguration.ExpectedDataStream( stubSequenceDeviceRoleName, STUB_DATA_POINT_TYPE )

            val configuration = DataStreamsConfiguration( stubDeploymentId, setOf( expectedStream ) )
            openDataStreams( configuration )
        }
}
