package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Tests for [ResourceRef] and [ResourceKind] serialization roundtrip compatibility.
 */
class ResourceRefTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_ResourceKind()
    {
        val resourceKinds = ResourceKind.entries.toTypedArray()

        resourceKinds.forEach { kind ->
            val serialized = json.encodeToString(kind)
            val deserialized = json.decodeFromString<ResourceKind>(serialized)
            assertEquals(kind, deserialized)
        }
    }

    @Test
    fun can_serialize_and_deserialize_ResourceRef_with_all_fields()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "logs/output.log",
            mediaType = "text/plain",
            byteSize = 1024L
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_ResourceRef_with_minimal_data()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.URI,
            value = "https://example.com/resource"
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertNull(deserialized.mediaType)
        assertNull(deserialized.byteSize)
    }

    @Test
    fun can_serialize_ResourceRef_with_RELATIVE_PATH()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "data/results.csv",
            mediaType = "text/csv",
            byteSize = 2048L
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertTrue(serialized.contains("RELATIVE_PATH"))
        assertTrue(serialized.contains("data/results.csv"))
    }

    @Test
    fun can_serialize_ResourceRef_with_URI()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.URI,
            value = "s3://bucket/logs/execution.log",
            mediaType = "application/octet-stream",
            byteSize = 4096L
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertTrue(serialized.contains("URI"))
        assertTrue(serialized.contains("s3://bucket"))
    }

    @Test
    fun serialization_preserves_all_ResourceKind_values()
    {
        val allResourceKinds = ResourceKind.entries.toList()

        val serialized = json.encodeToString(allResourceKinds)
        val deserialized = json.decodeFromString<List<ResourceKind>>(serialized)

        assertEquals(allResourceKinds, deserialized)
    }

    @Test
    fun can_serialize_ResourceRef_with_zero_byte_size()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "empty.txt",
            byteSize = 0L
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertEquals(0L, deserialized.byteSize)
    }

    @Test
    fun can_serialize_ResourceRef_with_large_byte_size()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.URI,
            value = "https://example.com/large-file.bin",
            byteSize = Long.MAX_VALUE
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertEquals(Long.MAX_VALUE, deserialized.byteSize)
    }

    @Test
    fun can_serialize_ResourceRef_with_special_characters_in_value()
    {
        val resourceRef = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "logs with spaces/file-with-dashes_and_underscores.log",
            mediaType = "text/plain"
        )

        val serialized = json.encodeToString(resourceRef)
        val deserialized = json.decodeFromString<ResourceRef>(serialized)

        assertEquals(resourceRef, deserialized)
        assertEquals("logs with spaces/file-with-dashes_and_underscores.log", deserialized.value)
    }
}
