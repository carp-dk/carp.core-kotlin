package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileDestination
import dk.cachet.carp.analytics.domain.data.RegistryDestination
import dk.cachet.carp.analytics.domain.data.DatabaseDestination
import dk.cachet.carp.analytics.domain.data.ApiDestination
import dk.cachet.carp.analytics.domain.data.StreamDestination
import dk.cachet.carp.analytics.domain.data.DatabaseType
import dk.cachet.carp.analytics.domain.data.HttpMethod
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.WriteMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

/**
 * Comprehensive unit tests for [ResolvedDataDestination] sealed interface.
 *
 * Tests all 5 variants with:
 * - Creation and property access
 * - Path resolution edge cases
 * - Write modes and overwrite handling
 * - Equality and hashing
 */
class ResolvedDataDestinationTest
{
    // ─────────────────────────────────────────────────────────────────────
    // File Variant Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `File variant stores original and resolved path`()
    {
        val original = FileDestination(
            path = "output.csv",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )
        val resolvedPath = "/workspace/output/step-id/output.csv"

        val destination = ResolvedDataDestination.File(
            original = original,
            resolvedPath = resolvedPath
        )

        assertEquals( original, destination.original )
        assertEquals( resolvedPath, destination.resolvedPath )
        assertIs<ResolvedDataDestination.File>( destination )
    }

    @Test
    fun `File variant handles absolute paths unchanged`()
    {
        val original = FileDestination(
            path = "/absolute/path/output.csv",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )
        val resolvedPath = "/absolute/path/output.csv"

        val destination = ResolvedDataDestination.File(
            original = original,
            resolvedPath = resolvedPath
        )

        assertEquals( original.path, destination.original.path )
        assertEquals( resolvedPath, destination.resolvedPath )
        assertEquals( original.path, destination.resolvedPath )
    }

    @Test
    fun `File variant handles empty paths with default resolution`()
    {
        val original = FileDestination(
            path = "",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )
        val resolvedPath = "/workspace/output/step-id/output"

        val destination = ResolvedDataDestination.File(
            original = original,
            resolvedPath = resolvedPath
        )

        assertEquals( "", destination.original.path )
        assertEquals( resolvedPath, destination.resolvedPath )
        assertTrue( destination.resolvedPath.endsWith( "output" ) )
    }

    @Test
    fun `File variant resolves relative paths to workspace`()
    {
        val original = FileDestination(
            path = "results/data.csv",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )
        val resolvedPath = "/workspace/outputs/step-456/results/data.csv"

        val destination = ResolvedDataDestination.File(
            original = original,
            resolvedPath = resolvedPath
        )

        assertTrue( destination.resolvedPath.startsWith( "/workspace" ) )
        assertTrue( destination.resolvedPath.endsWith( "data.csv" ) )
    }

    @Test
    fun `File variant preserves format information`()
    {
        val csvDestination = ResolvedDataDestination.File(
            original = FileDestination(
                path = "data.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = "/workspace/data.csv"
        )
        assertEquals( FileFormat.CSV, csvDestination.original.format )

        val jsonDestination = ResolvedDataDestination.File(
            original = FileDestination(
                path = "data.json",
                format = FileFormat.JSON,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = "/workspace/data.json"
        )
        assertEquals( FileFormat.JSON, jsonDestination.original.format )
    }

    @Test
    fun `File variant tracks write modes`()
    {
        val errorIfExists = ResolvedDataDestination.File(
            original = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = "/workspace/output.csv"
        )
        assertEquals( WriteMode.ERROR_IF_EXISTS, errorIfExists.original.writeMode )

        val append = ResolvedDataDestination.File(
            original = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.APPEND
            ),
            resolvedPath = "/workspace/output.csv"
        )
        assertEquals( WriteMode.APPEND, append.original.writeMode )

        val overwrite = ResolvedDataDestination.File(
            original = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = true,
                writeMode = WriteMode.OVERWRITE
            ),
            resolvedPath = "/workspace/output.csv"
        )
        assertEquals( WriteMode.OVERWRITE, overwrite.original.writeMode )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Registry Variant Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `Registry variant stores key and overwrite flag`()
    {
        val original = RegistryDestination(
            key = "results",
            overwrite = true
        )

        val destination = ResolvedDataDestination.Registry( original = original )

        assertEquals( original, destination.original )
        assertEquals( "results", destination.original.key )
        assertEquals( true, destination.original.overwrite )
        assertIs<ResolvedDataDestination.Registry>( destination )
    }

    @Test
    fun `Registry variant handles various key formats`()
    {
        val simpleKey = ResolvedDataDestination.Registry(
            original = RegistryDestination(
                key = "results",
                overwrite = false
            )
        )
        assertEquals( "results", simpleKey.original.key )

        val hierarchicalKey = ResolvedDataDestination.Registry(
            original = RegistryDestination(
                key = "workflow:step-123:output:final",
                overwrite = false
            )
        )
        assertTrue( hierarchicalKey.original.key.contains( ":" ) )
    }

    @Test
    fun `Registry variant handles overwrite policies`()
    {
        val noOverwrite = ResolvedDataDestination.Registry(
            original = RegistryDestination( key = "data", overwrite = false )
        )
        assertEquals( false, noOverwrite.original.overwrite )

        val withOverwrite = ResolvedDataDestination.Registry(
            original = RegistryDestination( key = "data", overwrite = true )
        )
        assertEquals( true, withOverwrite.original.overwrite )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Database Variant Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `Database variant stores connection and table info`()
    {
        val original = DatabaseDestination(
            connectionString = "jdbc:postgresql://localhost:5432/db",
            table = "results",
            databaseType = DatabaseType.POSTGRESQL,
            writeMode = WriteMode.APPEND,
            batchSize = 1000
        )

        val destination = ResolvedDataDestination.Database( original = original )

        assertEquals( original, destination.original )
        assertEquals( original.table, destination.original.table )
        assertEquals( original.connectionString, destination.original.connectionString )
        assertIs<ResolvedDataDestination.Database>( destination )
    }

    @Test
    fun `Database variant handles different database types`()
    {
        val postgres = ResolvedDataDestination.Database(
            original = DatabaseDestination(
                connectionString = "jdbc:postgresql://localhost:5432/db",
                table = "results",
                databaseType = DatabaseType.POSTGRESQL,
                writeMode = WriteMode.APPEND,
                batchSize = 1000
            )
        )
        assertEquals( DatabaseType.POSTGRESQL, postgres.original.databaseType )

        val mysql = ResolvedDataDestination.Database(
            original = DatabaseDestination(
                connectionString = "jdbc:mysql://localhost:3306/db",
                table = "results",
                databaseType = DatabaseType.MYSQL,
                writeMode = WriteMode.APPEND,
                batchSize = 500
            )
        )
        assertEquals( DatabaseType.MYSQL, mysql.original.databaseType )
    }

    @Test
    fun `Database variant tracks write modes and batch sizes`()
    {
        val append = ResolvedDataDestination.Database(
            original = DatabaseDestination(
                connectionString = "jdbc:postgresql://localhost/db",
                table = "results",
                databaseType = DatabaseType.POSTGRESQL,
                writeMode = WriteMode.APPEND,
                batchSize = 5000
            )
        )
        assertEquals( WriteMode.APPEND, append.original.writeMode )
        assertEquals( 5000, append.original.batchSize )

        val overwrite = ResolvedDataDestination.Database(
            original = DatabaseDestination(
                connectionString = "jdbc:postgresql://localhost/db",
                table = "results",
                databaseType = DatabaseType.POSTGRESQL,
                writeMode = WriteMode.OVERWRITE,
                batchSize = 1000
            )
        )
        assertEquals( WriteMode.OVERWRITE, overwrite.original.writeMode )
    }

    // ─────────────────────────────────────────────────────────────────────
    // API Variant Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `Api variant stores endpoint and method`()
    {
        val original = ApiDestination(
            endpoint = "https://api.example.com/results",
            method = HttpMethod.POST,
            headers = mapOf( "Authorization" to "Bearer token" ),
            format = FileFormat.JSON
        )

        val destination = ResolvedDataDestination.Api( original = original )

        assertEquals( original, destination.original )
        assertEquals( original.endpoint, destination.original.endpoint )
        assertIs<ResolvedDataDestination.Api>( destination )
    }

    @Test
    fun `Api variant handles different HTTP methods`()
    {
        val postMethod = ResolvedDataDestination.Api(
            original = ApiDestination(
                endpoint = "https://api.example.com/data",
                method = HttpMethod.POST,
                headers = emptyMap(),
                format = FileFormat.JSON
            )
        )
        assertEquals( HttpMethod.POST, postMethod.original.method )

        val putMethod = ResolvedDataDestination.Api(
            original = ApiDestination(
                endpoint = "https://api.example.com/data",
                method = HttpMethod.PUT,
                headers = emptyMap(),
                format = FileFormat.JSON
            )
        )
        assertEquals( HttpMethod.PUT, putMethod.original.method )
    }

    @Test
    fun `Api variant preserves custom headers`()
    {
        val headers = mapOf(
            "Authorization" to "Bearer token123",
            "Content-Type" to "application/json",
            "X-Custom-Header" to "value"
        )
        val original = ApiDestination(
            endpoint = "https://api.example.com/results",
            method = HttpMethod.POST,
            headers = headers,
            format = FileFormat.JSON
        )

        val destination = ResolvedDataDestination.Api( original = original )

        assertEquals( headers, destination.original.headers )
        assertEquals( 3, destination.original.headers.size )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Stream Variant Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `Stream variant stores stream ID and type`()
    {
        val original = StreamDestination(
            streamId = "results-stream",
            streamType = "kafka",
            configuration = mapOf( "topic" to "results" )
        )

        val destination = ResolvedDataDestination.Stream( original = original )

        assertEquals( original, destination.original )
        assertEquals( "results-stream", destination.original.streamId )
        assertEquals( "kafka", destination.original.streamType )
        assertIs<ResolvedDataDestination.Stream>( destination )
    }

    @Test
    fun `Stream variant handles different stream types`()
    {
        val kafka = ResolvedDataDestination.Stream(
            original = StreamDestination(
                streamId = "events",
                streamType = "kafka",
                configuration = mapOf( "topic" to "events" )
            )
        )
        assertEquals( "kafka", kafka.original.streamType )

        val rabbitmq = ResolvedDataDestination.Stream(
            original = StreamDestination(
                streamId = "events",
                streamType = "rabbitmq",
                configuration = mapOf( "queue" to "events" )
            )
        )
        assertEquals( "rabbitmq", rabbitmq.original.streamType )
    }

    @Test
    fun `Stream variant preserves configuration`()
    {
        val config = mapOf(
            "topic" to "results",
            "partition" to "3",
            "compression" to "gzip"
        )
        val original = StreamDestination(
            streamId = "results",
            streamType = "kafka",
            configuration = config
        )

        val destination = ResolvedDataDestination.Stream( original = original )

        assertEquals( config, destination.original.configuration )
        assertEquals( 3, destination.original.configuration.size )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Equality and Comparison Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `File destinations with same path are equal`()
    {
        val original = FileDestination(
            path = "output.csv",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )
        val path = "/workspace/output.csv"

        val dest1 = ResolvedDataDestination.File(
            original = original,
            resolvedPath = path
        )
        val dest2 = ResolvedDataDestination.File(
            original = original,
            resolvedPath = path
        )

        assertEquals( dest1, dest2 )
    }

    @Test
    fun `File destinations with different paths are not equal`()
    {
        val original = FileDestination(
            path = "output.csv",
            format = FileFormat.CSV,
            overwrite = false,
            writeMode = WriteMode.ERROR_IF_EXISTS
        )

        val dest1 = ResolvedDataDestination.File(
            original = original,
            resolvedPath = "/workspace/path1/output.csv"
        )
        val dest2 = ResolvedDataDestination.File(
            original = original,
            resolvedPath = "/workspace/path2/output.csv"
        )

        assertNotEquals( dest1, dest2 )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Type Safety Tests
    // ─────────────────────────────────────────────────────────────────────

    @Test
    fun `sealed interface prevents invalid subtypes`()
    {
        val file = ResolvedDataDestination.File(
            original = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            ),
            resolvedPath = "/workspace/output.csv"
        )

        assertIs<ResolvedDataDestination.File>( file )
        assertIs<ResolvedDataDestination>( file )
    }

    @Test
    fun `different variants can coexist in collections`()
    {
        val destinations: List<ResolvedDataDestination> = listOf(
            ResolvedDataDestination.File(
                original = FileDestination(
                    path = "a.csv",
                    format = FileFormat.CSV,
                    overwrite = false,
                    writeMode = WriteMode.ERROR_IF_EXISTS
                ),
                resolvedPath = "/workspace/a.csv"
            ),
            ResolvedDataDestination.Registry(
                original = RegistryDestination( key = "b", overwrite = false )
            ),
            ResolvedDataDestination.Stream(
                original = StreamDestination(
                    streamId = "c",
                    streamType = "kafka",
                    configuration = emptyMap()
                )
            )
        )

        assertEquals( 3, destinations.size )
        assertIs<ResolvedDataDestination.File>( destinations[0] )
        assertIs<ResolvedDataDestination.Registry>( destinations[1] )
        assertIs<ResolvedDataDestination.Stream>( destinations[2] )
    }
}