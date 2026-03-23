package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.DataSchema
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Comprehensive unit tests for [ResolvedInput] and [ResolvedOutput].
 *
 * Tests:
 * - Creation with specs and resolved sources/destinations
 * - Accessing spec and resolved properties
 * - Validation (blank name check)
 * - Field preservation across creation
 * - Error message clarity
 */
class ResolvedInputTest
{
    private fun createTestSpec(
        id: UUID = UUID.randomUUID(),
        name: String = "test-input",
        description: String = "Test input"
    ): InputDataSpec
    {
        return InputDataSpec(
            id = id,
            name = name,
            description = description,
            schema = DataSchema(
                format = FileFormat.CSV,
                encoding = "UTF-8"
            ),
            source = FileSystemSource(
                path = "test.csv",
                format = FileFormat.CSV
            ),
            required = true
        )
    }

    private fun createTestResolvedSource(): ResolvedDataSource
    {
        return ResolvedDataSource.FileSystem(
            original = FileSystemSource(
                path = "test.csv",
                format = FileFormat.CSV
            ),
            resolvedPath = "/workspace/input/test.csv"
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Creation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedInput creation with valid spec and source succeeds`()
    {
        val spec = createTestSpec()
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertEquals( spec, resolved.spec )
        assertEquals( source, resolved.resolvedSource )
    }

    @Test
    fun `ResolvedInput preserves all spec metadata`()
    {
        val inputId = UUID.randomUUID()
        val spec = createTestSpec(
            id = inputId,
            name = "my-input",
            description = "My input description"
        )
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertEquals( inputId, resolved.spec.id )
        assertEquals( "my-input", resolved.spec.name )
        assertEquals( "My input description", resolved.spec.description )
        assertEquals( true, resolved.spec.required )
    }

    @Test
    fun `ResolvedInput exposes spec schema information`()
    {
        val spec = createTestSpec()
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertNotNull( resolved.spec.schema )
        assertEquals( FileFormat.CSV, resolved.spec.schema.format)
        assertEquals( "UTF-8", resolved.spec.schema.encoding)
    }

    // ─────────────────────────────────────────────────────────────────────
    // Source Access Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedInput provides access to resolved source properties`()
    {
        val spec = createTestSpec()
        val resolvedPath = "/workspace/input/specific/path/data.csv"
        val source = ResolvedDataSource.FileSystem(
            original = FileSystemSource(
                path = "data.csv",
                format = FileFormat.CSV
            ),
            resolvedPath = resolvedPath
        )

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertIs<ResolvedDataSource.FileSystem>( resolved.resolvedSource )
        assertEquals( resolvedPath, resolved.resolvedSource.resolvedPath )
        assertEquals( FileFormat.CSV, (resolved.resolvedSource).original.format )
    }

    @Test
    fun `ResolvedInput can work with different source types`()
    {
        val spec = createTestSpec()
        val urlSource = ResolvedDataSource.Url(
            original = dk.cachet.carp.analytics.domain.data.UrlSource(
                url = "https://example.com/data.csv",
                format = FileFormat.CSV
            )
        )

        val resolved = ResolvedInput( spec = spec, resolvedSource = urlSource )

        assertIs<ResolvedDataSource.Url>( resolved.resolvedSource )
        assertEquals( "https://example.com/data.csv", (resolved.resolvedSource).original.url )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Validation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedInput throws when spec name is empty string`()
    {
        val spec = createTestSpec( name = "" )
        val source = createTestResolvedSource()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedInput( spec = spec, resolvedSource = source )
        }

        assertEquals( "Input spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedInput throws when spec name is whitespace only`()
    {
        val spec = createTestSpec( name = "   " )
        val source = createTestResolvedSource()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedInput( spec = spec, resolvedSource = source )
        }

        assertEquals( "Input spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedInput throws when spec name contains only tabs`()
    {
        val spec = createTestSpec( name = "\t\t\t" )
        val source = createTestResolvedSource()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedInput( spec = spec, resolvedSource = source )
        }

        assertEquals( "Input spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedInput throws when spec name contains only newlines`()
    {
        val spec = createTestSpec( name = "\n\n\n" )
        val source = createTestResolvedSource()

        val exception = assertFailsWith<IllegalArgumentException> {
            ResolvedInput( spec = spec, resolvedSource = source )
        }

        assertEquals( "Input spec name must not be blank", exception.message )
    }

    @Test
    fun `ResolvedInput accepts spec name with leading or trailing spaces if non-blank`()
    {
        // Note: depends on .isNotBlank() semantics - spaces make it non-blank
        val spec = createTestSpec( name = " input " )
        val source = createTestResolvedSource()

        // This should succeed because .isNotBlank() returns true for " input "
        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertEquals( " input ", resolved.spec.name )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Field Preservation Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedInput preserves spec schema through construction`()
    {
        val spec = InputDataSpec(
            id = UUID.randomUUID(),
            name = "input",
            description = "Test",
            schema = DataSchema(
                format = FileFormat.JSON,
                encoding = "UTF-16"
            ),
            source = FileSystemSource(
                path = "test.json",
                format = FileFormat.JSON
            ),
            required = false
        )
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertNotNull( resolved.spec.schema )
        assertEquals( FileFormat.JSON, resolved.spec.schema.format )
        assertEquals( "UTF-16", resolved.spec.schema.encoding )
    }

    @Test
    fun `ResolvedInput preserves source through access chain`()
    {
        val spec = createTestSpec()
        val originalPath = "/workspace/input/deep/nested/path/file.csv"
        val source = ResolvedDataSource.FileSystem(
            original = FileSystemSource(
                path = "file.csv",
                format = FileFormat.CSV
            ),
            resolvedPath = originalPath
        )

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        // Access through the resolved input
        val retrievedSource = resolved.resolvedSource as ResolvedDataSource.FileSystem
        assertEquals( originalPath, retrievedSource.resolvedPath )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Different Spec Types Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `ResolvedInput works with optional input`()
    {
        val spec = createTestSpec().copy( required = false )
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertEquals( false, resolved.spec.required )
    }

    @Test
    fun `ResolvedInput works with required input`()
    {
        val spec = createTestSpec().copy( required = true )
        val source = createTestResolvedSource()

        val resolved = ResolvedInput( spec = spec, resolvedSource = source )

        assertEquals( true, resolved.spec.required )
    }
}

