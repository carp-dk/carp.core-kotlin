package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue


/**
 * Tests for [InputDataSpec] and [OutputDataSpec].
 */
class DataReferenceTest
{
    private val json = createDefaultJSON( CoreAnalyticsSerializer.module )

    private fun input( location: DataLocation, stepRef: String? = null ) =
        InputDataSpec( UUID.randomUUID(), "in", location = location, stepRef = stepRef )

    private fun output( location: DataLocation, format: FileFormat? = null ) =
        OutputDataSpec( UUID.randomUUID(), "out", location = location, format = format )

    private fun errorsOf( result: DataSpecValidationResult ): List<String> =
        ( result as? DataSpecValidationResult.Invalid )?.errors.orEmpty()

    @Test
    fun can_serialize_and_deserialize_specs()
    {
        val input = InputDataSpec(
            UUID.randomUUID(),
            "in",
            description = "raw",
            schema = DataSchema( FileFormat.CSV ),
            location = FileLocation( "in.csv", FileFormat.CSV ),
            constraints = DataConstraints( minSize = 1, allowedFormats = listOf( "csv" ) )
        )
        val output = output( FileLocation( "out.json", FileFormat.JSON ), FileFormat.JSON )

        assertEquals( input, json.decodeFromString( InputDataSpec.serializer(), json.encodeToString( InputDataSpec.serializer(), input ) ) )
        assertEquals( output, json.decodeFromString( OutputDataSpec.serializer(), json.encodeToString( OutputDataSpec.serializer(), output ) ) )
    }

    @Test
    fun valid_input_specs_pass()
    {
        val valid = listOf(
            input( FileLocation( "in.csv" ) ),
            input( FileLocation(), stepRef = "previous" ),
            input( InMemoryLocation( "key" ) ),
            input( UrlLocation( "https://x" ) ),
            input( DatabaseLocation( "jdbc:x", "t" ) ),
            input( ApiLocation( "https://x" ) ),
            input( StreamLocation( "kafka://x", "topic" ) )
        )

        valid.forEach { assertIs<DataSpecValidationResult.Valid>( it.validate() ) }
    }

    @Test
    fun external_file_input_needs_a_path()
    {
        val result = input( FileLocation() ).validate()

        assertFalse( result.isValid )
        assertEquals( 1, errorsOf( result ).size )
    }

    @Test
    fun blank_stepRef_fails()
    {
        assertFalse( input( FileLocation( "in.csv" ), stepRef = " " ).validate().isValid )
    }

    @Test
    fun blank_location_fields_fail_for_inputs()
    {
        assertEquals( 1, errorsOf( input( InMemoryLocation( "" ) ).validate() ).size )
        assertEquals( 1, errorsOf( input( UrlLocation( "" ) ).validate() ).size )
        assertEquals( 2, errorsOf( input( DatabaseLocation( "", "" ) ).validate() ).size )
        assertEquals( 1, errorsOf( input( ApiLocation( "" ) ).validate() ).size )
        assertEquals( 2, errorsOf( input( StreamLocation( "", "" ) ).validate() ).size )
    }

    @Test
    fun output_needs_a_format()
    {
        assertFalse( output( FileLocation( "out" ) ).validate().isValid )
        assertTrue( output( FileLocation( "out" ), FileFormat.CSV ).validate().isValid )
        assertTrue( output( FileLocation( "out.csv", FileFormat.CSV ) ).validate().isValid )
    }

    @Test
    fun blank_location_fields_fail_for_outputs()
    {
        assertEquals( 1, errorsOf( output( InMemoryLocation( "" ) ).validate() ).size )
        assertEquals( 1, errorsOf( output( UrlLocation( "" ) ).validate() ).size )
        assertEquals( 2, errorsOf( output( DatabaseLocation( "", "" ) ).validate() ).size )
        assertEquals( 1, errorsOf( output( ApiLocation( "" ) ).validate() ).size )
        assertEquals( 2, errorsOf( output( StreamLocation( "", "" ) ).validate() ).size )
        assertIs<DataSpecValidationResult.Valid>( output( StreamLocation( "kafka://x", "t" ) ).validate() )
    }
}
