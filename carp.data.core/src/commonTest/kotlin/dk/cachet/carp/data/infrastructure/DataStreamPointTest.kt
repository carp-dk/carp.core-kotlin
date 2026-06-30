package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.DataStreamPoint
import dk.cachet.carp.data.application.SyncPoint
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests for [DataStreamPoint] relying on core infrastructure.
 */
class DataStreamPointTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )
    private val now = Clock.System.now()
    private val testDataStreamPoint = DataStreamPoint(
        0,
        UUID.randomUUID(),
        "Device",
        measurement( StubDataPoint(), 0 ),
        listOf( 0 ),
        SyncPoint( now, now.toEpochMicroseconds() )
    )

    @Test
    fun can_serialize_and_deserialize_DataStreamPoint()
    {
        val dataStreamPoint: DataStreamPoint<StubDataPoint> = testDataStreamPoint
        val serialized = json.encodeToString( dataStreamPoint )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: DataStreamPoint<StubDataPoint> =
            json.decodeFromString( serialized ) as DataStreamPoint<StubDataPoint>

        assertEquals( dataStreamPoint, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_DataStreamPoint_polymorphic()
    {
        val dataStreamPoint: DataStreamPoint<Data> = testDataStreamPoint
        val serialized = json.encodeToString( dataStreamPoint )
        val parsed: DataStreamPoint<Data> = json.decodeFromString( serialized )

        assertEquals( dataStreamPoint, parsed )
    }
}
