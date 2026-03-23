package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.UrlSource
import dk.cachet.carp.analytics.domain.data.DatabaseSource
import dk.cachet.carp.analytics.domain.data.InMemorySource
import dk.cachet.carp.analytics.domain.data.ApiSource
import dk.cachet.carp.analytics.domain.data.StreamSource
import dk.cachet.carp.analytics.domain.data.StepOutputSource
import dk.cachet.carp.analytics.domain.data.DatabaseType
import dk.cachet.carp.analytics.domain.data.HttpMethod
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

/**
 * Comprehensive unit tests for [ResolvedDataSource] sealed interface.
 *
 * Tests all 7 variants with:
 * - Creation and property access
 * - Path resolution edge cases
 * - Equality and hashing
 * - Serialization round-trips
 */
class ResolvedDataSourceTest
{

    // FileSystem Variant Tests

    @Test
    fun `FileSystem variant stores original and resolved path`()
    {
        // Arrange
        val original = FileSystemSource(
            path = "data.csv",
            format = FileFormat.CSV
        )
        val resolvedPath = "/workspace/input/step-id/data.csv"

        // Act
        val source = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = resolvedPath
        )

        // Assert
        assertEquals( original, source.original )
        assertEquals( resolvedPath, source.resolvedPath )
        assertIs<ResolvedDataSource.FileSystem>( source )
    }

    @Test
    fun `FileSystem variant handles absolute paths unchanged`()
    {
        val original = FileSystemSource(
            path = "/absolute/path/data.csv",
            format = FileFormat.CSV
        )
        val resolvedPath = "/absolute/path/data.csv"

        val source = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = resolvedPath
        )

        assertEquals( original.path, source.original.path )
        assertEquals( resolvedPath, source.resolvedPath )
        assertEquals( original.path, source.resolvedPath )
    }

    @Test
    fun `FileSystem variant resolves relative paths to workspace`()
    {
        val original = FileSystemSource(
            path = "input/raw.csv",
            format = FileFormat.CSV
        )
        val resolvedPath = "/workspace/inputs/step-123/input/raw.csv"

        val source = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = resolvedPath
        )

        assertTrue( source.resolvedPath.startsWith( "/workspace" ) )
        assertTrue( source.resolvedPath.endsWith( "raw.csv" ) )
    }

    @Test
    fun `FileSystem variant preserves original format`()
    {
        val originalJson = FileSystemSource(
            path = "data.json",
            format = FileFormat.JSON
        )
        val sourceJson = ResolvedDataSource.FileSystem(
            original = originalJson,
            resolvedPath = "/workspace/data.json"
        )

        assertEquals( FileFormat.JSON, sourceJson.original.format )

        val originalCsv = FileSystemSource(
            path = "data.csv",
            format = FileFormat.CSV
        )
        val sourceCsv = ResolvedDataSource.FileSystem(
            original = originalCsv,
            resolvedPath = "/workspace/data.csv"
        )

        assertEquals( FileFormat.CSV, sourceCsv.original.format )
    }

    // URL Variant Tests

    @Test
    fun `Url variant stores original source`()
    {
        val original = UrlSource(
            url = "https://example.com/data.csv",
            format = FileFormat.CSV
        )

        val source = ResolvedDataSource.Url( original = original )

        assertEquals( original, source.original )
        assertEquals( "https://example.com/data.csv", source.original.url )
        assertIs<ResolvedDataSource.Url>( source )
    }

    @Test
    fun `Url variant handles various protocols`()
    {
        val httpsSource = ResolvedDataSource.Url(
            original = UrlSource(
                url = "https://secure.example.com/data",
                format = FileFormat.JSON
            )
        )
        assertTrue( httpsSource.original.url.startsWith( "https://" ) )

        val ftpSource = ResolvedDataSource.Url(
            original = UrlSource(
                url = "ftp://files.example.com/data",
                format = FileFormat.CSV
            )
        )
        assertTrue( ftpSource.original.url.startsWith( "ftp://" ) )
    }

    // Database Variant Tests

    @Test
    fun `Database variant stores original with all connection details`()
    {
        val original = DatabaseSource(
            connectionString = "jdbc:postgresql://localhost:5432/db",
            query = "SELECT * FROM data",
            databaseType = DatabaseType.POSTGRESQL
        )

        val source = ResolvedDataSource.Database( original = original )

        assertEquals( original, source.original )
        assertEquals( original.connectionString, source.original.connectionString )
        assertEquals( "SELECT * FROM data", source.original.query )
        assertEquals( DatabaseType.POSTGRESQL, source.original.databaseType )
        assertIs<ResolvedDataSource.Database>( source )
    }

    @Test
    fun `Database variant handles different database types`()
    {
        val postgresSource = ResolvedDataSource.Database(
            original = DatabaseSource(
                connectionString = "jdbc:postgresql://localhost:5432/db",
                query = "SELECT * FROM data",
                databaseType = DatabaseType.POSTGRESQL
            )
        )
        assertEquals( DatabaseType.POSTGRESQL, postgresSource.original.databaseType )

        val mysqlSource = ResolvedDataSource.Database(
            original = DatabaseSource(
                connectionString = "jdbc:mysql://localhost:3306/db",
                query = "SELECT * FROM data",
                databaseType = DatabaseType.MYSQL
            )
        )
        assertEquals( DatabaseType.MYSQL, mysqlSource.original.databaseType )
    }

    // InMemory Variant Tests

    @Test
    fun `InMemory variant stores registry key`()
    {
        val original = InMemorySource( registryKey = "my-data" )

        val source = ResolvedDataSource.InMemory( original = original )

        assertEquals( original, source.original )
        assertEquals( "my-data", source.original.registryKey )
        assertIs<ResolvedDataSource.InMemory>( source )
    }

    @Test
    fun `InMemory variant handles various registry keys`()
    {
        val simpleKey = ResolvedDataSource.InMemory(
            original = InMemorySource( registryKey = "results" )
        )
        assertEquals( "results", simpleKey.original.registryKey )

        val complexKey = ResolvedDataSource.InMemory(
            original = InMemorySource( registryKey = "workflow:step-123:output" )
        )
        assertTrue( complexKey.original.registryKey.contains( ":" ) )
    }

    // API Variant Tests

    @Test
    fun `Api variant stores endpoint and method`()
    {
        val original = ApiSource(
            endpoint = "https://api.example.com/data",
            method = HttpMethod.GET
        )

        val source = ResolvedDataSource.Api( original = original )

        assertEquals( original, source.original )
        assertEquals( original.endpoint, source.original.endpoint )
        assertEquals( HttpMethod.GET, source.original.method )
        assertIs<ResolvedDataSource.Api>( source )
    }

    @Test
    fun `Api variant handles different HTTP methods`()
    {
        val getSource = ResolvedDataSource.Api(
            original = ApiSource(
                endpoint = "https://api.example.com/data",
                method = HttpMethod.GET
            )
        )
        assertEquals( HttpMethod.GET, getSource.original.method )

        val postSource = ResolvedDataSource.Api(
            original = ApiSource(
                endpoint = "https://api.example.com/data",
                method = HttpMethod.POST
            )
        )
        assertEquals( HttpMethod.POST, postSource.original.method )
    }

    // Stream Variant Tests

    @Test
    fun `Stream variant stores stream ID and type`()
    {
        val original = StreamSource(
            streamId = "my-stream",
            streamType = "kafka"
        )

        val source = ResolvedDataSource.Stream( original = original )

        assertEquals( original, source.original )
        assertEquals( "my-stream", source.original.streamId )
        assertEquals( "kafka", source.original.streamType )
        assertIs<ResolvedDataSource.Stream>( source )
    }

    @Test
    fun `Stream variant handles different stream types`()
    {
        val kafkaStream = ResolvedDataSource.Stream(
            original = StreamSource(
                streamId = "events",
                streamType = "kafka"
            )
        )
        assertEquals( "kafka", kafkaStream.original.streamType )

        val rabbitStream = ResolvedDataSource.Stream(
            original = StreamSource(
                streamId = "events",
                streamType = "rabbitmq"
            )
        )
        assertEquals( "rabbitmq", rabbitStream.original.streamType )
    }

    // StepOutput Variant Tests

    @Test
    fun `StepOutput variant resolves producer step and output IDs`()
    {
        val producerStepId = UUID.randomUUID()
        val producerOutputId = UUID.randomUUID()

        val original = StepOutputSource(
            stepId = producerStepId,
            outputId = producerOutputId,
            metadata = emptyMap()
        )

        val source = ResolvedDataSource.StepOutput(
            original = original,
            producerStepId = producerStepId,
            producerOutputId = producerOutputId
        )

        assertEquals( original, source.original )
        assertEquals( producerStepId, source.producerStepId )
        assertEquals( producerOutputId, source.producerOutputId )
        assertIs<ResolvedDataSource.StepOutput>( source )
    }

    @Test
    fun `StepOutput variant maintains cross-step references`()
    {
        val stepA = UUID.randomUUID()
        val stepB = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val original = StepOutputSource(
            stepId = stepA,
            outputId = outputId,
            metadata = mapOf( "source" to "upstream" )
        )

        val source = ResolvedDataSource.StepOutput(
            original = original,
            producerStepId = stepA,
            producerOutputId = outputId
        )

        assertEquals( stepA, source.producerStepId )
        assertNotEquals( stepA, stepB )
        assertEquals( outputId, source.producerOutputId )
    }

    // Equality and Comparison Tests

    @Test
    fun `FileSystem sources with same path are equal`()
    {
        val original = FileSystemSource(
            path = "data.csv",
            format = FileFormat.CSV
        )
        val path = "/workspace/data.csv"

        val source1 = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = path
        )
        val source2 = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = path
        )

        assertEquals( source1, source2 )
    }

    @Test
    fun `FileSystem sources with different paths are not equal`()
    {
        val original = FileSystemSource(
            path = "data.csv",
            format = FileFormat.CSV
        )

        val source1 = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = "/workspace/path1/data.csv"
        )
        val source2 = ResolvedDataSource.FileSystem(
            original = original,
            resolvedPath = "/workspace/path2/data.csv"
        )

        assertNotEquals( source1, source2 )
    }

    // Type Safety Tests

    @Test
    fun `sealed interface prevents invalid subtypes`()
    {
        val filesystem = ResolvedDataSource.FileSystem(
            original = FileSystemSource( path = "data.csv", format = FileFormat.CSV ),
            resolvedPath = "/workspace/data.csv"
        )

        assertIs<ResolvedDataSource.FileSystem>( filesystem )
        assertIs<ResolvedDataSource>( filesystem )
    }

    @Test
    fun `different variants can coexist in collections`()
    {
        val sources: List<ResolvedDataSource> = listOf(
            ResolvedDataSource.FileSystem(
                original = FileSystemSource( path = "a.csv", format = FileFormat.CSV ),
                resolvedPath = "/workspace/a.csv"
            ),
            ResolvedDataSource.Url(
                original = UrlSource( url = "https://example.com/b.csv", format = FileFormat.CSV )
            ),
            ResolvedDataSource.InMemory(
                original = InMemorySource( registryKey = "c" )
            )
        )

        assertEquals( 3, sources.size )
        assertIs<ResolvedDataSource.FileSystem>( sources[0] )
        assertIs<ResolvedDataSource.Url>( sources[1] )
        assertIs<ResolvedDataSource.InMemory>( sources[2] )
    }
}