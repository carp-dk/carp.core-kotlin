package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.application.execution.OutputRef
import dk.cachet.carp.analytics.domain.data.DataSchema
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.InMemorySource
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Registry for managing data artifacts during workflow execution.
 *
 * Acts as an internal cache where workflow steps can store and access intermediate
 * results by logical names. It supports in-memory datasets as well as file-based outputs,
 * and is essential for linking data flow between steps.
 */
@Serializable
class DataRegistry
{

    private val data = mutableMapOf<String, DataHandle>()

    /**
     * Register a new artifact under a logical name.
     * @throws IllegalArgumentException if name already exists.
     */
    fun register( name: String, artifact: DataHandle )
    {
        require(!data.containsKey(name)) { "Data already registered: $name" }
        data[name] = artifact
    }

    /**
     * Resolve an artifact by name.
     * @throws IllegalArgumentException if name does not exist.
     */
    fun resolve( name: String ): DataHandle
    {
        return data[name] ?: throw IllegalArgumentException("No data registered with name '$name'.")
    }

    /**
     * Check if a name is registered.
     */
    fun isRegistered( name: String ): Boolean
    {
        return data.containsKey(name)
    }

    /**
     *  Overwrite an existing registration (or insert if not present)
     */
    fun overwrite( name: String, artifact: DataHandle )
    {
        data[name] = artifact
    }

    /**
     * Return a list of structured outputs based on the current registry state.
     *
     * Converts all entries into [OutputRef], resolving memory vs file-based handles.
     */
    fun toExecutionOutputs(): List<OutputRef>
    {
        return data.map { (name, handle) ->
            val result: OutputRef = when (handle) {
                is FileData -> OutputRef(
                    outputId = UUID.randomUUID(), // Generate UUID for each output
                    source = FileSystemSource(
                        path = handle.path,
                        format = inferFormat(handle.path, handle.mimeType)
                    ),
                    format = inferFormat(handle.path, handle.mimeType),
                    schema = DataSchema(format = inferFormat(handle.path, handle.mimeType))
                )
                is InMemoryData -> OutputRef(
                    outputId = UUID.randomUUID(), // Generate UUID for each output
                    source = InMemorySource(registryKey = name),
                    format = FileFormat.JSON, // Default format for in-memory data
                    schema = DataSchema(format = FileFormat.JSON)
                )
                else -> throw IllegalArgumentException("Unknown DataHandle type: ${handle::class.simpleName}")
            }
            result
        }
    }

    /**
     * Infer file format from path or MIME type.
     */
    private fun inferFormat( path: String, mimeType: String? ): FileFormat
    {
        // Try MIME type first
        if (mimeType != null)
        {
            return when {
                mimeType.contains("csv") -> FileFormat.CSV
                mimeType.contains("json") -> FileFormat.JSON
                mimeType.contains("xml") -> FileFormat.XML
                mimeType.contains("excel") || mimeType.contains("spreadsheet") -> FileFormat.EXCEL
                mimeType.contains("yaml") -> FileFormat.YAML
                mimeType.contains("parquet") -> FileFormat.PARQUET
                mimeType.contains("avro") -> FileFormat.AVRO
                else -> inferFromExtension(path)
            }
        }

        // Fall back to file extension
        return inferFromExtension(path)
    }

    private fun inferFromExtension( path: String ): FileFormat
    {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "csv" -> FileFormat.CSV
            "json" -> FileFormat.JSON
            "xml" -> FileFormat.XML
            "parquet" -> FileFormat.PARQUET
            "avro" -> FileFormat.AVRO
            "xlsx", "xls" -> FileFormat.EXCEL
            "yaml", "yml" -> FileFormat.YAML
            "tsv" -> FileFormat.TSV
            else -> FileFormat.BINARY
        }
    }
}
