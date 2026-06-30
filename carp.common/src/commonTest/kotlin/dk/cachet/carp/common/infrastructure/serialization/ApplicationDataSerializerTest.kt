package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.ApplicationData
import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for [ApplicationDataSerializer].
 */
class ApplicationDataSerializerTest
{
    @Serializable
    data class ContainsApplicationData(
        val normalData: String,
        val applicationData: ApplicationData?
    )

    private val json = createDefaultJSON()


    @Test
    fun can_serialize_and_deserialize_non_json_application_data()
    {
        val toSerialize = ContainsApplicationData( "normal", ApplicationData( "some application data" ) )

        val serialized = json.encodeToString( toSerialize )
        val parsed: ContainsApplicationData = json.decodeFromString( serialized )
        assertEquals( toSerialize, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_json_application_data()
    {
        // Parse application data as JSON to remove newlines/white spaces.
        val applicationData = json.parseToJsonElement(
            """
            {
                "key": "Some value",
                "key2": {
                    "nested": "object"
                }
            }
            """
        )
        val toSerialize = ContainsApplicationData( "normal", ApplicationData( applicationData.toString() ) )

        val serialized = json.encodeToString( toSerialize )
        val parsed: ContainsApplicationData = json.decodeFromString( serialized )
        assertEquals( toSerialize, parsed )
    }

    @Test
    fun can_serialize_and_deserialize_null_application_data()
    {
        val toSerialize = ContainsApplicationData( "normal", null )

        val serialized = json.encodeToString( toSerialize )
        val parsed: ContainsApplicationData = json.decodeFromString( serialized )
        assertEquals( toSerialize, parsed )
    }

    @Test
    fun json_serializer_serializes_as_json_element()
    {
        val toSerialize = ContainsApplicationData( "normal", ApplicationData( """{"json":"data"}""" ) )

        val serialized = json.encodeToString( toSerialize )

        // The application data should not be escaped, but simply be added as plain JSON.
        assertFalse( serialized.contains( '\\' ) )
    }

    @Test
    fun can_serialize_malformed_json()
    {
        val malformedJson = """{"json object":"or not?"""
        val toSerialize = ContainsApplicationData( "normal", ApplicationData( malformedJson ) )

        val serialized = json.encodeToString( toSerialize )

        // Fallback for malformed JSON is serializing it as an escaped string.
        assertTrue( serialized.contains( '\\' ) )
    }
}
