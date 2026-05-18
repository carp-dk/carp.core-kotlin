package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import dk.cachet.carp.common.infrastructure.test.STUBS_SERIAL_MODULE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.MeasurementSerializer
import kotlin.test.*


/**
 * Tests for [Measurement] relying on core infrastructure.
 */
class MeasurementTest
{
    private val json = createDefaultJSON( STUBS_SERIAL_MODULE )


    @Test
    fun can_serialize_and_deserialize_Measurement()
    {
        val measurement: Measurement<StubDataPoint> = measurement( StubDataPoint(), 0 )
        val serialized = json.encodeToString( MeasurementSerializer, measurement )
        @Suppress( "UNCHECKED_CAST" )
        val parsed: Measurement<StubDataPoint> =
            json.decodeFromString( MeasurementSerializer, serialized ) as Measurement<StubDataPoint>

        assertEquals( measurement, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_Measurement_polymorphic()
    {
        val measurement: Measurement<Data> = measurement( StubDataPoint(), 0 )
        val serialized = json.encodeToString( MeasurementSerializer, measurement )
        val parsed: Measurement<Data> = json.decodeFromString( MeasurementSerializer, serialized )

        assertEquals( measurement, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_Measurement_with_unknown_data()
    {
        // Construct JSON of measurement with an unknown data type.
        val stubData = StubDataPoint( "Some data" )
        val measurement: Measurement<Data> = measurement( stubData, 0 )
        val encoded = json.encodeToString( measurement )
        val measurementWithUnknownData = encoded.makeUnknown( stubData )

        // Deserializing the measurement with unknown data type and serializing it should result in the original JSON.
        val parsed: Measurement<Data> = json.decodeFromString( MeasurementSerializer, measurementWithUnknownData )
        val serialized = json.encodeToString( MeasurementSerializer, parsed )
        assertEquals( measurementWithUnknownData, serialized )
    }
}
