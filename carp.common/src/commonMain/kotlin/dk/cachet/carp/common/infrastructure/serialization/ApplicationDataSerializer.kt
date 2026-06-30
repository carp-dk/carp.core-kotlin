package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.ApplicationData
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*


/**
 * Tries to serialize the data contained in [ApplicationData] as a [JsonElement],
 * but only in case a JSON encoder/decoder is used.
 * In case the JSON contained in the text is malformed, it will be serialized as a normal escaped string.
 */
@OptIn( ExperimentalSerializationApi::class )
class ApplicationDataSerializer : KSerializer<ApplicationData?>
{
    override val descriptor: SerialDescriptor = String.serializer().nullable.descriptor

    override fun deserialize( decoder: Decoder ): ApplicationData?
    {
        // Early out when the value is null.
        if ( !decoder.decodeNotNullMark() ) return null

        // Application data is only serialized as JSON for JSON encoder.
        if ( decoder !is JsonDecoder ) return ApplicationData( decoder.decodeSerializableValue( String.serializer() ) )

        // Read application data which is stored as JSON.
        val jsonElement = decoder.decodeJsonElement()
        val originalString = jsonElement.toString()

        // In case application data was a primitive string, trim the surrounding quotes.
        val data =
            if ( jsonElement is JsonObject ) originalString
            else originalString.substring( 1, originalString.length - 1 )

        return ApplicationData( data )
    }

    override fun serialize( encoder: Encoder, value: ApplicationData? )
    {
        // Early out when the value is null.
        if ( value == null )
        {
            encoder.encodeNull()
            return
        }

        // Application data is only serialized as JSON for JSON encoder.
        if ( encoder !is JsonEncoder )
        {
            encoder.encodeNullableSerializableValue( String.serializer().nullable, value.data )
            return
        }

        val json = encoder.json
        var isJsonObject = value.data.startsWith( "{" )
        if ( isJsonObject )
        {
            try
            {
                val jsonElement = json.parseToJsonElement( value.data )
                encoder.encodeJsonElement( jsonElement )
            }
            catch( _: SerializationException )
            {
                isJsonObject = false
            }
        }

        if ( !isJsonObject ) encoder.encodeString( value.data )
    }
}
