package dk.cachet.carp.common.application.data.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * Holds custom input data as requested by a researcher.
 */
@SerialName( CUSTOM_INPUT_TYPE_NAME )
@Suppress( "Immutable" ) // TODO: `assumeImmutable` configuration in detekt.yml is not working.
@JsExport
data class CustomInput( val input: Any ) : Data


/**
 * A serializer for [CustomInput] which supports serializing input of registered data types.
 *
 * @param supportedDataTypes The input data types this serializer can serialize.
 *   A serializer should be registered for these classes.
 */
@OptIn( ExperimentalSerializationApi::class, InternalSerializationApi::class )
class CustomInputSerializer( vararg supportedDataTypes: KClass<*> ) : KSerializer<CustomInput>
{
    // TODO: Can we use the fully qualified type name to register serializers here, like kotlinx.serialization does? How?
    val dataTypeMap: Map<String, KSerializer<out Any>> =
        supportedDataTypes.associate { it.simpleName!! to it.serializer() }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor( CUSTOM_INPUT_TYPE_NAME )
    {
        element<String>( "dataType" )
        element( "input", buildSerialDescriptor("$CUSTOM_INPUT_TYPE_NAME.value", SerialKind.CONTEXTUAL ) )
    }

    override fun deserialize( decoder: Decoder ): CustomInput =
        decoder.decodeStructure( descriptor )
        {
            // Read dataType.
            val index = decodeElementIndex( descriptor )
            require( index == 0 ) { "Cannot read input before its data type." }
            val dataType = decodeStringElement( descriptor, 0 )

            // Read input
            decodeElementIndex( descriptor )
            val serializer = getRegisteredSerializer( dataType )
            val input = decodeSerializableElement( descriptor, 1, serializer )

            CustomInput( input )
        }

    override fun serialize( encoder: Encoder, value: CustomInput ) =
        encoder.encodeStructure( descriptor )
        {
            val input = value.input
            val inputKlass = input::class.simpleName!!

            encodeStringElement( descriptor, 0, inputKlass )

            @Suppress( "UNCHECKED_CAST" )
            val serializer = getRegisteredSerializer( inputKlass ) as KSerializer<Any>
            encodeSerializableElement( descriptor, 1, serializer, input )
        }

    fun getRegisteredSerializer( dataType: String ): KSerializer<out Any> = dataTypeMap[ dataType ]
        ?: throw UnsupportedOperationException( "No serializer registered for custom input data type: $dataType" )
}


internal const val CUSTOM_INPUT_TYPE_NAME = "${CarpInputDataTypes.CARP_NAMESPACE}.custom"
