package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.infrastructure.reflect.reflectIfAvailable
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass


/**
 * A serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param wrapperClass The definition of the class [W] which is used to wrap objects of type [P].
 * @param verifyUnknownPolymorphicWrapper
 *  For this serializer to work, all wrapper classes returned by this serializer need to implement [UnknownPolymorphicWrapper].
 *  In case it is impossible for a base return type to implement this interface you can disable the runtime verification by setting this to false.
 *  However, ensure that all deriving classes of this base type implement [UnknownPolymorphicWrapper], otherwise serialization will not output the original JSON found upon deserializing.
 */
abstract class UnknownPolymorphicSerializer<P : Any, W : P>(
    baseClass: KClass<P>,
    wrapperClass: KClass<W>,
    verifyUnknownPolymorphicWrapper: Boolean = true
) : KSerializer<P>
{
    companion object
    {
        private val unsupportedException =
            SerializationException(
                "${UnknownPolymorphicSerializer::class.simpleName} only supports JSON serialization, " +
                "configured to use a class discriminator for polymorphism."
            )
    }

    init
    {
        // Enforce that wrapper type (W) implements UnknownPolymorphicWrapper.
        // Due to current limitations in Kotlin (stemming from the JVM) this cannot be statically enforced: https://stackoverflow.com/q/43790137/590790
        val reflect = reflectIfAvailable()
        if ( reflect != null && verifyUnknownPolymorphicWrapper )
        {
            val implementsInterface: Boolean = reflect.extendsType<UnknownPolymorphicWrapper>( wrapperClass )
            require( implementsInterface ) { "'$wrapperClass' must implement '${UnknownPolymorphicWrapper::class}'." }
        }
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "dk.cachet.carp.common.infrastructure.serialization.UnknownPolymorphicSerializer<${baseClass.simpleName}>"
    )


    override fun serialize( encoder: Encoder, value: P )
    {
        // This serializer assumes JSON serialization with class discriminator configured for polymorphism.
        // TODO: It should also be possible to support array polymorphism, but that is not a priority now.
        if ( encoder !is JsonEncoder )
        {
            throw unsupportedException
        }
        val classDiscriminator = getClassDiscriminator( encoder.json ) // Throws error in case of array polymorphism.

        // Get the unknown JSON object.
        check( value is UnknownPolymorphicWrapper )
        val unknown = Json.parseToJsonElement( value.jsonSource ) as JsonObject
        val unknownTypeFields = unknown.filter { it.key != classDiscriminator }

        // Create a serial descriptor which contains all elements of the unknown JSON object, except type discriminator.
        // If the encoder is in polymorphic writing mode, a class discriminator field will automatically be added using
        // the serial name of the descriptor.
        val unknownType = checkNotNull( unknown[ classDiscriminator ]?.jsonPrimitive?.content )
        val jsonSerializer = JsonElement.serializer()
        val overrideDescriptor = buildClassSerialDescriptor( unknownType )
        {
            unknownTypeFields.keys.forEach { element( it, jsonSerializer.descriptor ) }
        }

        // Write the JSON object.
        val overrideEncoder = encoder.encodeInline( overrideDescriptor )
        overrideEncoder.encodeStructure( overrideDescriptor )
        {
            var id = 0
            for ( field in unknownTypeFields.values )
                encodeSerializableElement( overrideDescriptor, id++, JsonElement.serializer(), field )
        }
    }

    override fun deserialize( decoder: Decoder ): P
    {
        // This serializer assumes JSON serialization with class discriminator configured for polymorphism.
        // TODO: It should also be possible to support array polymorphism, but that is not a priority now.
        if ( decoder !is JsonDecoder )
        {
            throw unsupportedException
        }
        val classDiscriminator = getClassDiscriminator( decoder.json )

        // Get raw JSON for the unknown type.
        val jsonElement = decoder.decodeJsonElement()
        val jsonSource = jsonElement.toString()
        val className = requireNotNull( jsonElement.jsonObject[ classDiscriminator ]?.jsonPrimitive?.content )
            { "Can't deserialize type which was serialized non-polymorphically." }

        return createWrapper( className, jsonSource, decoder.json )
    }

    private fun getClassDiscriminator( json: Json ): String
    {
        if ( json.configuration.useArrayPolymorphism ) throw unsupportedException

        return json.configuration.classDiscriminator
    }

    /**
     * Create a wrapper for a class which could not be deserialized since it is not in any loaded assembly.
     *
     * @param className The fully qualified name of the class.
     * @param json The JSON which could not be deserialized.
     * @param serializer The [Json] serializer being used to deserialize the object.
     */
    abstract fun createWrapper( className: String, json: String, serializer: Json ): W
}


/**
 * Create a serializer for polymorph objects of type [P] which wraps extending types unknown at runtime as instances of type [W].
 *
 * @param createWrapper Create the wrapper based on the fully qualified name of the class and the JSON which could not be deserialized.
 */
inline fun <reified P : Any, reified W : P> createUnknownPolymorphicSerializer(
    crossinline createWrapper: (className: String, json: String, serializer: Json) -> W
): UnknownPolymorphicSerializer<P, W>
{
    return object : UnknownPolymorphicSerializer<P, W>( P::class, W::class )
    {
        override fun createWrapper( className: String, json: String, serializer: Json ): W
        {
            return createWrapper( className, json, serializer )
        }
    }
}
