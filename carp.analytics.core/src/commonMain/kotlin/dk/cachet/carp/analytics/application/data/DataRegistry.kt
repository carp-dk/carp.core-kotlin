package dk.cachet.carp.analytics.application.data

import kotlinx.serialization.Serializable

/**
 * Registry for managing data artefacts during workflow execution.
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
     * Register a new artefact under a logical name.
     * @throws IllegalArgumentException if name already exists.
     */
    fun register( name: String, artifact: DataHandle )
    {
        require(!data.containsKey(name)) { "Data already registered: $name" }
        data[name] = artifact
    }

    /**
     * Resolve an artefact by name.
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
}
