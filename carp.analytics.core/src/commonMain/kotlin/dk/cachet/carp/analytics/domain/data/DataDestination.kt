package dk.cachet.carp.analytics.domain.data import kotlinx.serialization.Serializable

/**
 * Represents a destination where processed data can be written.
 * This is a sealed interface to provide type-safe destination configurations.
 */
@Serializable
sealed interface DataDestination
{
    /** The type of this destination */
    val destinationType: DestinationType
}

/**
 * File system destination for writing data.
 *
 * @property path The full file system path where data should be written
 * @property format The format to use when writing the data
 * @property overwrite Whether to overwrite existing data
 * @property writeMode How to handle existing data
 */
@Serializable
data class FileDestination(
    val path: String,
    val format: FileFormat,
    val overwrite: Boolean = false,
    val writeMode: WriteMode = WriteMode.ERROR_IF_EXISTS
) : DataDestination
{
    override val destinationType: DestinationType = DestinationType.FILE_SYSTEM
}

/**
 * In-memory registry destination for storing data.
 *
 * @property key The registry key where data should be stored
 * @property overwrite Whether to overwrite existing data in the registry
 */
@Serializable
data class RegistryDestination(
    val key: String,
    val overwrite: Boolean = true
) : DataDestination
{
    override val destinationType: DestinationType = DestinationType.REGISTRY
}

/**
 * Database destination for writing data.
 *
 * @property connectionString Database connection string
 * @property table The table or collection name
 * @property databaseType The type of database
 * @property writeMode How to handle existing data
 * @property batchSize Number of records to write in each batch
 */
@Serializable
data class DatabaseDestination(
    val connectionString: String,
    val table: String,
    val databaseType: DatabaseType,
    val writeMode: WriteMode = WriteMode.APPEND,
    val batchSize: Int = 1000
) : DataDestination
{
    override val destinationType: DestinationType = DestinationType.DATABASE
}

/**
 * API destination for posting data.
 *
 * @property endpoint The API endpoint URL
 * @property method The HTTP method to use
 * @property authentication Authentication configuration
 * @property headers HTTP headers
 * @property format Format for serializing data in the request body
 */
@Serializable
data class ApiDestination(
    val endpoint: String,
    val method: HttpMethod = HttpMethod.POST,
    val authentication: Authentication? = null,
    val headers: Map<String, String> = emptyMap(),
    val format: FileFormat = FileFormat.JSON
) : DataDestination
{
    override val destinationType: DestinationType = DestinationType.API
}

/**
 * Streaming destination for publishing data.
 *
 * @property streamId Identifier for the destination stream
 * @property streamType Type of streaming system
 * @property configuration Stream-specific configuration
 */
@Serializable
data class StreamDestination(
    val streamId: String,
    val streamType: String,
    val configuration: Map<String, String> = emptyMap()
) : DataDestination
{
    override val destinationType: DestinationType = DestinationType.STREAM
}
