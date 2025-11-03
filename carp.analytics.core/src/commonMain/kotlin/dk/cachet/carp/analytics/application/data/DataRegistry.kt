package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.ExecutionOutput
import dk.cachet.carp.analytics.domain.execution.ArtifactType
import dk.cachet.carp.analytics.domain.execution.ExecutionArtifact
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
     * Converts all entries into [ExecutionOutput], resolving memory vs file-based handles.
     */
    fun toExecutionOutputs(): List<ExecutionOutput> =
        data.mapNotNull { (name, handle) ->
            when (handle) {
                is FileData -> ExecutionOutput(
                    name = name,
                    dataType = "file",
                    location = DataLocation(listOf(handle.path), scheme = "file")
                )
                is InMemoryData -> ExecutionOutput(
                    name = name,
                    dataType = "dataset",
                    location = DataLocation(listOf("memory", name), scheme = "mem", isAbsolute = false)
                )
            }
        }
    /**
     * Return a list of [ExecutionArtifact].
     *
     * Only includes file-based artifacts registered in this instance.
     */
    fun toArtifacts(): List<ExecutionArtifact> =
        data.mapNotNull { (name, handle) ->
            if (handle is FileData)
                ExecutionArtifact(
                    uri = handle.path,
                    name = name,
                    type = ArtifactType.FILE,
                    mimeType = handle.mimeType
                )
            else null
        }
}
