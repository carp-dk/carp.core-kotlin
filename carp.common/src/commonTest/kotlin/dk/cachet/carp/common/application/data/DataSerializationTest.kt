package dk.cachet.carp.common.application.data

import dk.cachet.carp.common.infrastructure.serialization.CLASS_DISCRIMINATOR
import dk.cachet.carp.common.infrastructure.serialization.CustomData
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubDataTypes
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.*


/**
 * Tests for serializing [Data] types in [dk.cachet.carp.common.application.data].
 */
class DataSerializationTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_data_nonpolymorphically()
    {
        val data = StubDataPoint( "some text" )
        val serializer = StubDataPoint.serializer()

        val serialized = json.encodeToString( serializer, data )
        assertEquals( """{"data":"some text"}""", serialized )
    }

    @Test
    fun class_discriminator_of_serialized_data_equals_matching_data_type()
    {
        val data = StubDataPoint( "some text" )
        val serializer = PolymorphicSerializer( Data::class )

        val serialized = json.encodeToString( serializer, data )
        assertEquals(
            "{\"$CLASS_DISCRIMINATOR\":\"${StubDataTypes.STUB_DATA_POINT_TYPE_NAME}\",\"data\":\"some text\"}",
            serialized
        )
    }

    @Test
    fun sensor_specific_data_is_serialized_unknown_polymorphically()
    {
        val sensorSpecificData = StubDataPoint()
        val data = StubDataPoint( "some data", sensorSpecificData )

        val serialized = json.encodeToString( data )
        val unknownSerialized = serialized.makeUnknown( sensorSpecificData, "unknown" )
        val deserialized = json.decodeFromString<StubDataPoint>( unknownSerialized )

        val customSensorSpecificData = deserialized.sensorSpecificData as? CustomData
        assertNotNull( customSensorSpecificData )
        assertEquals( "unknown", customSensorSpecificData.className )
    }
}
