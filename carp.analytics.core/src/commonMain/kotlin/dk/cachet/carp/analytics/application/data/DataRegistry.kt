package dk.cachet.carp.analytics.application.data
import kotlinx.serialization.Serializable


/**
 * Registry for managing data artifacts during workflow execution.
 * Maps logical names to CollectedDataSets or file paths.
 */
@Serializable
class DataRegistry {

    private val data = mutableMapOf<String, DataHandle>()

    /**
     * Register a new artifact under a logical name.
     * @throws IllegalArgumentException if name already exists.
     */
    fun register(name: String, artifact: DataHandle) {
        if (data.containsKey(name)) {
            throw IllegalArgumentException("Data with name '$name' is already registered.")
        }
        data[name] = artifact
    }

    /**
     * Resolve an artifact by name.
     * @throws IllegalArgumentException if name does not exist.
     */
    fun resolve(name: String): DataHandle {
        return data[name] ?: throw IllegalArgumentException("No data registered with name '$name'.")
    }

    /**
     * Check if a name is registered.
     */
    fun isRegistered(name: String): Boolean {
        return data.containsKey(name)
    }

    /**
     * Optionally allow overwriting (if you want later)
     */
    fun overwrite(name: String, artifact: DataHandle) {
        data[name] = artifact
    }
}
