package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.data.*
import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


/**
 * Tests for [OutputRef] serialization roundtrip compatibility.
 */
class OutputRefTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_FileSystemSource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = FileSystemSource(
                path = "/data/output.csv",
                format = FileFormat.CSV
            ),
            format = FileFormat.CSV,
            schema = DataSchema(
                format = FileFormat.CSV,
                columns = listOf(
                    ColumnSpec("id", NamespacedId("dk.cachet.test", "string"), nullable = false),
                    ColumnSpec("value", NamespacedId("dk.cachet.test", "double"), nullable = true)
                )
            )
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_UrlSource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = UrlSource(
                url = "https://api.example.com/data.json",
                format = FileFormat.JSON
            ),
            format = FileFormat.JSON,
            schema = DataSchema(
                format = FileFormat.JSON,
                jsonSchema = """{"type": "object", "properties": {"name": {"type": "string"}}}"""
            )
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_DatabaseSource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = DatabaseSource(
                connectionString = "postgresql://user@host:5432/db",
                query = "SELECT * FROM results",
                databaseType = DatabaseType.POSTGRESQL
            ),
            format = FileFormat.JSON,
            schema = DataSchema(
                format = FileFormat.JSON,
                encoding = "UTF-8"
            )
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_ApiSource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = ApiSource(
                endpoint = "https://api.service.com/v1/results",
                method = HttpMethod.GET,
                headers = mapOf("Authorization" to "Bearer token123")
            ),
            format = FileFormat.JSON,
            schema = DataSchema(FileFormat.JSON)
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_minimal_schema()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = FileSystemSource("/tmp/simple.txt", FileFormat.JSON),
            format = FileFormat.JSON,
            schema = DataSchema(FileFormat.JSON)
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
        assertEquals(FileFormat.JSON, deserialized.schema.format)
        assertEquals("UTF-8", deserialized.schema.encoding)
        assertNull(deserialized.schema.columns)
        assertNull(deserialized.schema.jsonSchema)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_complex_schema()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = FileSystemSource("/data/complex.parquet", FileFormat.PARQUET),
            format = FileFormat.PARQUET,
            schema = DataSchema(
                format = FileFormat.PARQUET,
                columns = listOf(
                    ColumnSpec("user_id", NamespacedId("dk.cachet.test", "string"), nullable = false, description = "Unique user identifier"),
                    ColumnSpec("timestamp", NamespacedId("dk.cachet.test", "string"), nullable = false, description = "Event timestamp"),
                    ColumnSpec("event_data", NamespacedId("dk.cachet.test", "string"), nullable = true, description = "JSON event payload"),
                    ColumnSpec("score", NamespacedId("dk.cachet.test", "double"), nullable = true, defaultValue = "0.0")
                ),
                encoding = "UTF-8",
                compression = "snappy"
            )
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
        assertEquals(4, deserialized.schema.columns!!.size)
        assertEquals("snappy", deserialized.schema.compression)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_different_formats()
    {
        val formats = listOf(FileFormat.CSV, FileFormat.JSON, FileFormat.PARQUET, FileFormat.XML)

        formats.forEach { format ->
            val outputRef = OutputRef(
                outputId = UUID.randomUUID(),
                source = FileSystemSource("/data/test.$format", format),
                format = format,
                schema = DataSchema(format)
            )

            val serialized = json.encodeToString(outputRef)
            val deserialized = json.decodeFromString<OutputRef>(serialized)

            assertEquals(outputRef, deserialized)
            assertEquals(format, deserialized.format)
            assertEquals(format, deserialized.schema.format)
        }
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_InMemorySource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = InMemorySource(
                registryKey = "temp-data-key-123"
            ),
            format = FileFormat.JSON,
            schema = DataSchema(FileFormat.JSON)
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_OutputRef_with_StreamSource()
    {
        val outputRef = OutputRef(
            outputId = UUID.randomUUID(),
            source = StreamSource(
                streamId = "kafka-topic-results",
                streamType = "kafka",
                configuration = mapOf(
                    "bootstrap.servers" to "localhost:9092",
                    "key.serializer" to "string"
                )
            ),
            format = FileFormat.JSON,
            schema = DataSchema(FileFormat.JSON)
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(outputRef, deserialized)
    }

    @Test
    fun serialization_preserves_uuid_precision()
    {
        val specificUuid = UUID.parse("550e8400-e29b-41d4-a716-446655440000")
        val outputRef = OutputRef(
            outputId = specificUuid,
            source = FileSystemSource("/test.json", FileFormat.JSON),
            format = FileFormat.JSON,
            schema = DataSchema(FileFormat.JSON)
        )

        val serialized = json.encodeToString(outputRef)
        val deserialized = json.decodeFromString<OutputRef>(serialized)

        assertEquals(specificUuid, deserialized.outputId)
        assertEquals(outputRef, deserialized)
    }
}
