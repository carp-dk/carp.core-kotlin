package dk.cachet.carp.common.infrastructure.serialization

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.*


class CustomSerializerWrapperTest
{
    @Serializable
    sealed class SealedBase
    {
        @Serializable
        class ExtendsSealed( val answer: Int = 42 ) : SealedBase()
    }

    @Test
    fun can_enforce_serializing_as_polymorph_base_class()
    {
        val wrapper = customSerializerWrapper( SealedBase.ExtendsSealed(), SealedBase.serializer() )

        val serialized = Json.encodeToString( CustomSerializerWrapper.serializer(), wrapper )

        val jsonObject = Json.parseToJsonElement( serialized ) as JsonObject
        assertTrue( jsonObject.contains( "type" ) )
    }
}
