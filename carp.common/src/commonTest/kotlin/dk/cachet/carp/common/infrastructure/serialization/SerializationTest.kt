package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.commonInstances
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.elements.InputElement
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.sampling.BatteryAwareSamplingConfiguration
import dk.cachet.carp.common.application.sampling.Granularity
import dk.cachet.carp.common.application.sampling.GranularitySamplingConfiguration
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.infrastructure.test.*
import dk.cachet.carp.test.serialization.ConcreteTypesSerializationTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*


val testJson = createDefaultJSON( STUBS_SERIAL_MODULE )

val unknownInstances = listOf(
    // `infrastructure` namespace.
    unknown( StubDataPoint() ) { CustomData( it.first, it.second, it.third ) },
    unknown( StubDeviceConfiguration() ) { CustomDeviceConfiguration( it.first, it.second, it.third ) },
    unknown( StubPrimaryDeviceConfiguration() ) { CustomPrimaryDeviceConfiguration( it.first, it.second, it.third ) },
    unknown( DefaultDeviceRegistration() ) { CustomDeviceRegistration( it.first, it.second, it.third ) },
    unknown( StubSamplingConfiguration( "" ) ) { CustomSamplingConfiguration( it.first, it.second, it.third ) },
    unknown( StubTaskConfiguration() ) { CustomTaskConfiguration( it.first, it.second, it.third ) },
    unknown( StubTriggerConfiguration( "source" ) ) { CustomTriggerConfiguration( it.first, it.second, it.third ) },
)

/**
 * Convert the specified [stub] to the corresponding [UnknownPolymorphicWrapper] as if it were unknown at runtime.
 */
inline fun <reified Base : Any> unknown( stub: Base, constructor: (Triple<String, String, Json>) -> Base ): Base
{
    val originalObject = testJson.encodeToJsonElement( PolymorphicSerializer( Base::class ), stub ) as JsonObject

    // Mimic the passed stub as an unknown object.
    val unknownName = "Unknown"
    val unknownObject = originalObject.toMutableMap()
    unknownObject[ CLASS_DISCRIMINATOR ] = JsonPrimitive( unknownName )
    val jsonSource = testJson.encodeToString( unknownObject )

    return constructor( Triple( unknownName, jsonSource, testJson ) )
}


/**
 * Serialization tests for all extending types from base classes in [dk.cachet.carp.common].
 */
class SerializationTest : ConcreteTypesSerializationTest(
    testJson,
    COMMON_SERIAL_MODULE,
    commonInstances + unknownInstances
)
{
    @Test
    fun can_serialize_and_deserialize_polymorphic_InputElement()
    {
        val inputElement: InputElement<*> = Text( "Test" )
        val polySerializer = PolymorphicSerializer( InputElement::class )
        val serialized = json.encodeToString( polySerializer, inputElement )
        val parsed = json.decodeFromString( polySerializer, serialized )
        assertEquals( inputElement, parsed )
    }

    @Test
    fun can_serialize_polymorphic_CustomInput()
    {
        val data: Data = CustomInput( 42 )
        val dataSerializer = PolymorphicSerializer( Data::class )

        val serialized = json.encodeToString( dataSerializer, data )
        val parsed = json.decodeFromString( dataSerializer, serialized )
        assertEquals( data, parsed )
    }

    @Test
    fun can_serialize_polymorphic_BatteryAwareSamplingConfiguration()
    {
        val configuration: BatteryAwareSamplingConfiguration<GranularitySamplingConfiguration> =
            BatteryAwareSamplingConfiguration(
                GranularitySamplingConfiguration( Granularity.Balanced ),
                GranularitySamplingConfiguration( Granularity.Coarse ),
            )
        val serializer = PolymorphicSerializer( SamplingConfiguration::class )

        val serialized = json.encodeToString( serializer, configuration )
        val parsed = json.decodeFromString( serializer, serialized )
        assertEquals( configuration, parsed )
    }
}
