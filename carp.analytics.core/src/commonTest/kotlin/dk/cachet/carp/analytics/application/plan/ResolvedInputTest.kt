package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.FileLocation
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Comprehensive unit tests for [ResolvedInput] using unified DataLocation model.
 *
 * Tests:
 * - Creation with specs and resolved locations
 * - Accessing spec and location properties
 * - Validation (spec name check)
 * - Field preservation across creation
 * - External vs step-based inputs (stepRef null vs non-null)
 * - Location path access via helper method
 */
class ResolvedInputTest
{
    private fun createTestSpec(
        id: UUID = UUID.randomUUID(),
        name: String = "test-input",
        description: String = "Test input",
        stepRef: String? = null // null = external, non-null = from step
    ): InputDataSpec
    {
        return InputDataSpec(
            id = id,
            name = name,
            description = description,
            location = FileLocation(
                path = if ( stepRef == null ) "test.csv" else "",
                format = FileFormat.CSV
            ),
            stepRef = stepRef,
            required = true
        )
    }

    private fun createTestLocation( path: String = "/workspace/input/test.csv" ): FileLocation
    {
        return FileLocation(
            path = path,
            format = FileFormat.CSV
        )
    }

    // Creation Tests

    @Test
    fun `ResolvedInput creation with valid spec and location succeeds`()
    {
        val spec = createTestSpec()
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( spec, resolved.spec )
        assertEquals( location, resolved.location )
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
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( inputId, resolved.spec.id )
        assertEquals( "my-input", resolved.spec.name )
        assertEquals( "My input description", resolved.spec.description )
        assertEquals( true, resolved.spec.required )
    }

    // Location Access Tests

    @Test
    fun `ResolvedInput provides access to unified DataLocation`()
    {
        val spec = createTestSpec()
        val resolvedPath = "/workspace/input/specific/path/data.csv"
        val location = FileLocation(
            path = resolvedPath,
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertIs<FileLocation>( resolved.location )
        assertEquals( resolvedPath, resolved.location.path )
        assertEquals( FileFormat.CSV, resolved.location.format )
    }

    @Test
    fun `ResolvedInput helper method getPath returns correct path`()
    {
        val spec = createTestSpec()
        val location = createTestLocation( "/workspace/test.csv" )

        val resolved = ResolvedInput( spec = spec, location = location )

        val path = resolved.getPath()
        assertNotNull( path )
        assertEquals( "/workspace/test.csv", path )
    }

    // External vs Step-Based Input Tests

    @Test
    fun `ResolvedInput for external input has null stepRef`()
    {
        val spec = createTestSpec( stepRef = null ) // External
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        assertNull( resolved.spec.stepRef )
        assertEquals( "/workspace/input/test.csv", ( resolved.location as FileLocation ).path )
    }

    @Test
    fun `ResolvedInput for step-based input has stepRef`()
    {
        val spec = createTestSpec( stepRef = "step-1" ) // From step-1
        val location = FileLocation(
            path = "/workspace/outputs/step-1/output.csv",
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( "step-1", resolved.spec.stepRef )
        assertEquals( "/workspace/outputs/step-1/output.csv", ( resolved.location as FileLocation ).path )
    }

    // Field Preservation Tests

    @Test
    fun `ResolvedInput preserves location through construction`()
    {
        val spec = createTestSpec()
        val originalPath = "/workspace/input/deep/nested/path/file.csv"
        val location = FileLocation(
            path = originalPath,
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        val retrievedPath = (resolved.location as FileLocation).path
        assertEquals( originalPath, retrievedPath )
    }

    // Different Location Format Tests

    @Test
    fun `ResolvedInput works with different file formats`()
    {
        val spec = InputDataSpec(
            id = UUID.randomUUID(),
            name = "json-input",
            location = FileLocation( path = "test.json", format = FileFormat.JSON ),
            required = true
        )
        val location = FileLocation(
            path = "/workspace/input/data.json",
            format = FileFormat.JSON
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( FileFormat.JSON, ( resolved.location as FileLocation ).format )
    }

    @Test
    fun `ResolvedInput works with required input`()
    {
        val spec = createTestSpec().copy( required = true )
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( true, resolved.spec.required )
    }

    @Test
    fun `ResolvedInput works with optional input`()
    {
        val spec = createTestSpec().copy( required = false )
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( false, resolved.spec.required )
    }

    // Data Origin Detection Tests

    @Test
    fun `detecting external input from null stepRef`()
    {
        val spec = createTestSpec( stepRef = null )
        val location = createTestLocation()

        val resolved = ResolvedInput( spec = spec, location = location )

        // External data: stepRef is null
        assertNull( resolved.spec.stepRef )
    }

    @Test
    fun `detecting step-produced input from non-null stepRef`()
    {
        val spec = createTestSpec( stepRef = "upstream-step" )
        val location = FileLocation(
            path = "/workspace/outputs/upstream-step/result.csv",
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        // Step-produced data: stepRef is non-null
        assertEquals( "upstream-step", resolved.spec.stepRef )
    }

    // Location Path Variants Tests

    @Test
    fun `ResolvedInput handles absolute paths`()
    {
        val spec = createTestSpec()
        val location = FileLocation(
            path = "/absolute/path/to/file.csv",
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( "/absolute/path/to/file.csv", resolved.getPath() )
    }

    @Test
    fun `ResolvedInput handles relative paths`()
    {
        val spec = createTestSpec()
        val location = FileLocation(
            path = "relative/path/to/file.csv",
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( "relative/path/to/file.csv", resolved.getPath() )
    }

    @Test
    fun `ResolvedInput handles workspace-relative paths`()
    {
        val spec = createTestSpec()
        val location = FileLocation(
            path = "/workspace/outputs/step-1/result.csv",
            format = FileFormat.CSV
        )

        val resolved = ResolvedInput( spec = spec, location = location )

        assertEquals( "/workspace/outputs/step-1/result.csv", resolved.getPath() )
    }
}
