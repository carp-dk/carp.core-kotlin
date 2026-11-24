package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed interface for all data location types.
 *
 * Represents where data is stored or retrieved from during workflow execution.
 *
 * **Location Types:**
 * - [FileLocation] — File system paths
 * - [InMemoryLocation] — In-memory registry/cache
 * - [UrlLocation] — Remote HTTP(S) endpoints
 * - [DatabaseLocation] — Database connections
 * - [ApiLocation] — REST/GraphQL API endpoints
 * - [StreamLocation] — Streaming sources (Kafka, etc.)
 */
@Serializable
sealed interface DataLocation
{
    /**
     * Metadata map for this location (source, destination, format hints, etc.).
     *
     * Used for debugging and auditing. Examples:
     * - `"source" to "file"` — came from YAML FileInputSource
     * - `"source" to "step-output"` — came from another step
     * - `"destination" to "file"` — write to file system
     * - `"format" to "csv"` — expected format hint
     */
    val metadata: Map<String, String>
}


// FILE LOCATION


/**
 * File system location.
 *
 * Represents data stored in files (local or network file systems).
 *
 * An absolute [path] is used as-is. A blank or relative [path] is placed in the step's output
 * directory of the run workspace by the executor.
 *
 * @property path File path (absolute, relative, or empty)
 * @property format File format enum (CSV, JSON, PARQUET, etc.)
 * @property metadata Source/destination metadata
 */
@Serializable
@SerialName( "file" )
data class FileLocation(
    val path: String = "",
    val format: FileFormat = FileFormat.BINARY,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


// IN-MEMORY LOCATION


/**
 * In-memory registry location.
 *
 * Represents data stored in memory, environment variables, or configuration registries.
 * Data is keyed by `registryKey` for lookup/storage during execution.
 *
 * **Examples:**
 * - Environment variables: `MODEL_PATH`, `DATA_DIR`
 * - Configuration parameters: `batch_size`, `learning_rate`
 * - In-process caches: Step outputs stored in memory between steps
 *
 * **Resolution:** Returns self (no path generation needed for in-memory storage).
 *
 * @property registryKey Unique key for this data in the registry
 * @property metadata Registry metadata
 */
@Serializable
@SerialName( "in-memory" )
data class InMemoryLocation(
    val registryKey: String,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


// URL LOCATION


/**
 * Remote URL location.
 *
 * Represents data accessible via HTTP(S) endpoints.
 *
 * **Examples:**
 * - `https://api.example.com/data.json` — REST API endpoint
 * - `https://storage.amazonaws.com/bucket/file.parquet` — S3-like storage
 * - `https://data-lake.company.com/datasets/sales.csv` — Data lake endpoint
 *
 * **Resolution:** Returns self (URL is already fully qualified).
 *
 * @property url Full URL (http:// or https://)
 * @property format Expected response format (for parsing)
 * @property metadata URL metadata (authentication type, timeout, etc.)
 */
@Serializable
@SerialName( "url" )
data class UrlLocation(
    val url: String,
    val format: FileFormat = FileFormat.BINARY,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


// DATABASE LOCATION


/**
 * Database location.
 *
 * Represents data stored in relational or document databases.
 *
 * **Examples:**
 * - PostgreSQL: `postgresql://host:5432/db?table=users`
 * - MongoDB: `mongodb://host:27017/db?collection=events`
 * - Snowflake: `snowflake://account/warehouse/db/schema?table=data`
 *
 * **Resolution:** Returns self (connection string is already set).
 *
 * @property connectionString Database connection URI
 * @property table Table/collection/entity name
 * @property format Expected data format (AVRO, PARQUET, etc. for schema)
 * @property metadata Database metadata (credentials ref, query params, etc.)
 */
@Serializable
@SerialName( "database" )
data class DatabaseLocation(
    val connectionString: String,
    val table: String,
    val format: FileFormat = FileFormat.BINARY,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


// API LOCATION


/**
 * API endpoint location.
 *
 * Represents data accessed through REST, GraphQL, or custom API endpoints.
 *
 * **Examples:**
 * - REST: `POST https://api.example.com/v1/analyze`
 * - GraphQL: `https://api.example.com/graphql (query: getMetrics)`
 * - gRPC: `grpc://service:50051/DataService/GetData`
 *
 * **Resolution:** Returns self (endpoint is already set).
 *
 * @property endpoint API endpoint URL
 * @property method HTTP method (GET, POST, etc.) or API method
 * @property format Response format (JSON, PROTOBUF, etc.)
 * @property metadata API metadata (auth token, request body, headers, timeout, etc.)
 */
@Serializable
@SerialName( "api" )
data class ApiLocation(
    val endpoint: String,
    val method: String = "GET",
    val format: FileFormat = FileFormat.BINARY,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


// STREAM LOCATION


/**
 * Streaming data source location.
 *
 * Represents continuous or message stream sources.
 *
 * **Examples:**
 * - Kafka: `kafka://broker:9092/topic=events`
 * - Kinesis: `kinesis://stream-name`
 * - Pub/Sub: `pubsub://project/subscription`
 * - WebSocket: `ws://live.example.com/stream`
 *
 * **Resolution:** Returns self (stream address is already set).
 *
 * @property streamAddress Stream broker/service address
 * @property topicOrStream Topic/stream/subscription name
 * @property format Message format (JSON, AVRO, Protobuf, etc.)
 * @property metadata Stream metadata (consumer group, offset, format schema, etc.)
 */
@Serializable
@SerialName( "stream" )
data class StreamLocation(
    val streamAddress: String,
    val topicOrStream: String,
    val format: FileFormat = FileFormat.BINARY,
    override val metadata: Map<String, String> = emptyMap()
) : DataLocation


