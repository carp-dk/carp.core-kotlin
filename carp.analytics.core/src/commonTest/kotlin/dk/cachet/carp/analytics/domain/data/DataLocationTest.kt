package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Tests for [DataLocation], [FileFormat] and [DataSchema].
 */
class DataLocationTest
{
    private val json = createDefaultJSON( CoreAnalyticsSerializer.module )

    @Test
    fun can_serialize_and_deserialize_all_locations()
    {
        val locations: List<DataLocation> = listOf(
            FileLocation( "data/in.csv", FileFormat.CSV, mapOf( "k" to "v" ) ),
            InMemoryLocation( "key" ),
            UrlLocation( "https://example.org/data.json", FileFormat.JSON ),
            DatabaseLocation( "jdbc:postgresql://db", "measurements" ),
            ApiLocation( "https://example.org/api", "POST" ),
            StreamLocation( "kafka://broker", "topic" )
        )

        locations.forEach { location ->
            val serialized = json.encodeToString( DataLocation.serializer(), location )
            assertEquals( location, json.decodeFromString( DataLocation.serializer(), serialized ) )
        }
    }

    @Test
    fun location_type_names_are_stable()
    {
        val serialized = json.encodeToString( DataLocation.serializer(), InMemoryLocation( "key" ) )

        assertTrue( "\"in-memory\"" in serialized )
    }

    @Test
    fun file_location_defaults_to_binary_without_path()
    {
        val location = FileLocation()

        assertEquals( "", location.path )
        assertEquals( FileFormat.BINARY, location.format )
        assertTrue( location.metadata.isEmpty() )
    }

    @Test
    fun file_format_isText_is_inverse_of_isBinary()
    {
        FileFormat.entries.forEach { assertEquals( !it.isBinary, it.isText ) }
        assertTrue( FileFormat.CSV.isText )
        assertFalse( FileFormat.PARQUET.isText )
    }

    @Test
    fun can_serialize_and_deserialize_DataSchema()
    {
        val schema = DataSchema(
            format = FileFormat.CSV,
            columns = listOf( ColumnSpec( "steps", DataType( "dk.cachet.carp", "stepcount" ), nullable = false ) ),
            compression = "gzip"
        )

        val serialized = json.encodeToString( DataSchema.serializer(), schema )

        assertEquals( schema, json.decodeFromString( DataSchema.serializer(), serialized ) )
    }

    @Test
    fun schemas_with_different_formats_are_incompatible()
    {
        assertFalse( DataSchema( FileFormat.CSV ).isCompatibleWith( DataSchema( FileFormat.JSON ) ) )
    }

    @Test
    fun schemas_without_columns_are_compatible()
    {
        assertTrue( DataSchema( FileFormat.CSV ).isCompatibleWith( DataSchema( FileFormat.CSV ) ) )
    }

    @Test
    fun schema_is_compatible_when_it_has_all_required_columns()
    {
        val type = DataType( "dk.cachet.carp", "x" )
        val provided = DataSchema( FileFormat.CSV, listOf( ColumnSpec( "a", type ), ColumnSpec( "b", type ) ) )
        val required = DataSchema(
            FileFormat.CSV,
            listOf( ColumnSpec( "a", type, nullable = false ), ColumnSpec( "c", type, nullable = true ) )
        )
        val missing = DataSchema( FileFormat.CSV, listOf( ColumnSpec( "c", type, nullable = false ) ) )

        assertTrue( provided.isCompatibleWith( required ) )
        assertFalse( provided.isCompatibleWith( missing ) )
    }
}
