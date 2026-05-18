package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.DataStreamServiceTest
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [InMemoryDataStreamService].
 */
class InMemoryDataStreamServiceTest : DataStreamServiceTest
{
    override fun createService(): DataStreamService = InMemoryDataStreamService()


    @Test
    fun appendToDataStreams_for_unknown_datatype_succeeds() = runTest {
        val service = createService()

        val deploymentId = UUID.randomUUID()
        val unknownType = DataType( "unknown", "type" )

        // Open data stream for unknown data type.
        val dataStreamId = DataStreamId( deploymentId, "Some device", unknownType )
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId( dataStreamId )
        val configuration = DataStreamsConfiguration( deploymentId, setOf( expectedStream ) )
        service.openDataStreams( configuration )

        // Create unknown data point.
        val json = createTestJSON()
        val dataPoint = StubDataPoint()
        val dataPointJson = json.encodeToString<Data>( dataPoint )
        val unknownDataPointJson = dataPointJson.makeUnknown( dataPoint )
        val unknownDataPoint: Data = json.decodeFromString( unknownDataPointJson )

        // Append data point.
        val now = Clock.System.now()
        val sequence = MutableDataStreamSequence<Data>(
            dataStreamId,
            0,
            listOf( 0 ),
            SyncPoint( now, now.toEpochMicroseconds() )
        )
        sequence.appendMeasurements( Measurement( 0, null, unknownType, unknownDataPoint ) )
        val batch = MutableDataStreamBatch()
        batch.appendSequence( sequence )
        service.appendToDataStreams( deploymentId, batch )

        val retrievedDataStream = service.getDataStream( dataStreamId, 0 )
        assertEquals( unknownDataPoint, retrievedDataStream.single().measurement.data )
    }
}
