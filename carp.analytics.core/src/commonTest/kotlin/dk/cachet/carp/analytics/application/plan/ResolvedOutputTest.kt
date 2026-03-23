package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.*
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Comprehensive unit tests for [ResolvedOutput].
 *
 * Tests:
 * - Creation with specs and resolved destinations
 * - Accessing spec and resolved properties
 * - Validation (blank name check)
 * - Field preservation across creation
 */
class ResolvedOutputTest
{
    private fun createTestSpec(
        id: UUID = UUID.randomUUID(),
        name: String = "test-output",
        description: String = "Test output"
    ): OutputDataSpec
    {
        return OutputDataSpec(
            id = id,
            name = name,
            description = description,
            schema = DataSchema(
                format = FileFormat.CSV,
                encoding = "UTF-8"
            ),
            destination = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            format = FileFormat.CSV
        )
    }

    private fun createTestResolvedDestination(): ResolvedDataDestination
    {
        return ResolvedDataDestination.File(
            original = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = "/workspace/output/output.csv"
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Creation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedOutput creation with valid spec and destination succeeds`()
    {
        val spec = createTestSpec()
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertEquals( spec, resolved.spec )
        assertEquals( destination, resolved.resolvedDestination )
    }

    @Test
    fun `ResolvedOutput preserves all spec metadata`()
    {
        val outputId = UUID.randomUUID()
        val spec = createTestSpec(
            id = outputId,
            name = "my-output",
            description = "My output description"
        )
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertEquals( outputId, resolved.spec.id )
        assertEquals( "my-output", resolved.spec.name )
        assertEquals( "My output description", resolved.spec.description )
        assertEquals( FileFormat.CSV, resolved.spec.format )
    }

    @Test
    fun `ResolvedOutput exposes spec schema information`()
    {
        val spec = createTestSpec()
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertNotNull( resolved.spec.schema )
        assertEquals( FileFormat.CSV, resolved.spec.schema.format)
        assertEquals( "UTF-8", resolved.spec.schema.encoding)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Destination Access Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedOutput provides access to resolved destination properties`()
    {
        val spec = createTestSpec()
        val resolvedPath = "/workspace/output/specific/path/results.csv"
        val destination = ResolvedDataDestination.File(
            original = FileDestination(
                path = "results.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = resolvedPath
        )

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertIs<ResolvedDataDestination.File>( resolved.resolvedDestination )
        assertEquals( resolvedPath, resolved.resolvedDestination.resolvedPath )
        assertEquals( FileFormat.CSV, resolved.resolvedDestination.original.format )
    }

    @Test
    fun `ResolvedOutput can work with different destination types`()
    {
        val spec = createTestSpec()
        val registryDest = ResolvedDataDestination.Registry(
            original = RegistryDestination(
                key = "results",
                overwrite = true
            )
        )

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = registryDest )

        assertIs<ResolvedDataDestination.Registry>( resolved.resolvedDestination )
        assertEquals( "results", resolved.resolvedDestination.original.key )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Validation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedOutput throws when spec name is empty string`()
    {
        val spec = createTestSpec( name = "" )
        val destination = createTestResolvedDestination()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedOutput( spec = spec, resolvedDestination = destination )
        }

        assertEquals( "Output spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedOutput throws when spec name is whitespace only`()
    {
        val spec = createTestSpec( name = "   " )
        val destination = createTestResolvedDestination()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedOutput( spec = spec, resolvedDestination = destination )
        }

        assertEquals( "Output spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedOutput throws when spec name contains only tabs and newlines`()
    {
        val spec = createTestSpec( name = "\t\n\t\n" )
        val destination = createTestResolvedDestination()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedOutput( spec = spec, resolvedDestination = destination )
        }

        assertEquals( "Output spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedOutput accepts spec name with leading or trailing spaces if non-blank`()
    {
        val spec = createTestSpec( name = " output " )
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertEquals( " output ", resolved.spec.name )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Field Preservation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedOutput preserves spec schema through construction`()
    {
        val spec = OutputDataSpec(
            id = UUID.randomUUID(),
            name = "output",
            description = "Test",
            schema = DataSchema(
                format = FileFormat.JSON,
                encoding = "UTF-16"
            ),
            destination = FileDestination(
                path = "output.json",
                format = FileFormat.JSON,
                overwrite = true,
                writeMode = WriteMode.OVERWRITE
            ),
            format = FileFormat.JSON
        )
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        assertNotNull( resolved.spec.schema )
        assertEquals( FileFormat.JSON, resolved.spec.schema.format)
        assertEquals( "UTF-16", resolved.spec.schema.encoding)
        assertEquals( FileFormat.JSON, resolved.spec.format )
    }

    @Test
    fun `ResolvedOutput preserves destination through access chain`()
    {
        val spec = createTestSpec()
        val originalPath = "/workspace/output/deep/nested/path/results.csv"
        val destination = ResolvedDataDestination.File(
            original = FileDestination(
                path = "results.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = originalPath
        )

        val resolved = ResolvedOutput( spec = spec, resolvedDestination = destination )

        val retrievedDest = resolved.resolvedDestination as ResolvedDataDestination.File
        assertEquals( originalPath, retrievedDest.resolvedPath )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Write Mode Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedOutput tracks write modes correctly`()
    {
        val errorIfExists = createTestSpec().copy(
            destination = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            )
        )
        val destination = createTestResolvedDestination()

        val resolved = ResolvedOutput( spec = errorIfExists, resolvedDestination = destination )

        assertEquals( WriteMode.ERROR_IF_EXISTS, ( resolved.spec.destination as FileDestination ).writeMode )
    }
}