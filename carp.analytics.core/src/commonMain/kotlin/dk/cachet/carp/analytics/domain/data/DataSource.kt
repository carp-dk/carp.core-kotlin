package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

/**
 * Represents a source from which data can be retrieved.
 * This is a sealed interface to provide type-safe data source configurations.
 */
@Serializable
sealed interface DataSource {
    /** The type of this data source */
    val sourceType: DataSourceType

    /** Additional metadata specific to this source (key-value string pairs) */
    val metadata: Map<String, String>
}

/**
 * File system-based data source.
 *
 * @property path The full file system path to the data
 * @property format The format of the file
 * @property metadata Additional metadata (e.g., encoding, compression)
 */
@Serializable
data class FileSystemSource(
    val path: String,
    val format: FileFormat,
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.FILE_SYSTEM
}

/**
 * HTTP/HTTPS URL-based data source.
 *
 * @property url The URL to retrieve data from
 * @property format The expected format of the response
 * @property headers HTTP headers to include in the request
 * @property metadata Additional metadata
 */
@Serializable
data class UrlSource(
    val url: String,
    val format: FileFormat,
    val headers: Map<String, String> = emptyMap(),
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.HTTP
}

/**
 * Database-based data source.
 *
 * @property connectionString Database connection string
 * @property query SQL or query language statement
 * @property databaseType The type of database
 * @property metadata Additional metadata (e.g., credentials, pool settings)
 */
@Serializable
data class DatabaseSource(
    val connectionString: String,
    val query: String,
    val databaseType: DatabaseType,
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.DATABASE
}

/**
 * In-memory data registry source.
 *
 * @property registryKey The key to look up data in the registry
 * @property metadata Additional metadata
 */
@Serializable
data class InMemorySource(
    val registryKey: String,
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.IN_MEMORY
}

/**
 * REST API or web service data source.
 *
 * @property endpoint The API endpoint URL
 * @property method The HTTP method to use
 * @property authentication Authentication configuration
 * @property parameters Query or body parameters
 * @property headers HTTP headers
 * @property metadata Additional metadata
 */
@Serializable
data class ApiSource(
    val endpoint: String,
    val method: HttpMethod = HttpMethod.GET,
    val authentication: Authentication? = null,
    val parameters: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.API
}

/**
 * Streaming data source configuration.
 *
 * @property streamId Identifier for the data stream
 * @property streamType Type of streaming system (Kafka, RabbitMQ, etc.)
 * @property configuration Stream-specific configuration
 * @property metadata Additional metadata
 */
@Serializable
data class StreamSource(
    val streamId: String,
    val streamType: String,
    val configuration: Map<String, String> = emptyMap(),
    override val metadata: Map<String, String> = emptyMap()
) : DataSource {
    override val sourceType: DataSourceType = DataSourceType.STREAM
}

/**
 * Authentication configuration for data sources.
 */
@Serializable
sealed interface Authentication {
    /** No authentication */
    @Serializable
    object None : Authentication

    /** Basic HTTP authentication */
    @Serializable
    data class Basic(val username: String, val password: String) : Authentication

    /** Bearer token authentication */
    @Serializable
    data class Bearer(val token: String) : Authentication

    /** API key authentication */
    @Serializable
    data class ApiKey(val key: String, val headerName: String = "X-API-Key") : Authentication

    /** OAuth2 authentication */
    @Serializable
    data class OAuth2(val accessToken: String, val refreshToken: String? = null) : Authentication
}

