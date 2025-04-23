package dk.cachet.carp.analytics.application.environment


import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.infrastructure.environment.CondaEnvironment


/**
 * Factory for creating specific environment instances.
 */

/**
 * EnvironmentFactory: Centralized Factory for Environment Creation
 *
 * **Design Overview:**
 * - The `EnvironmentFactory` is implemented as a singleton object to provide a centralized,
 *   immutable factory for creating environment instances (e.g., Conda, Venv).
 * - The factory uses a **static registry** (`Map<String, (Map<String, Any>) -> Environment>`) to
 *   pre-register all supported environment types during initialization.
 * - The registry is immutable (`val registry`), ensuring no modifications or dynamic additions
 *   can occur at runtime. All supported types are defined in the `init` block or `mapOf` declaration.
 * - The `create` function dynamically constructs an environment instance by invoking the creation
 *   function corresponding to the requested type.
 *
 * **Key Design Choices:**
 * 1. **Static Registry:**
 *    - Ensures that all supported environment types are pre-defined and unmodifiable.
 *    - Prevents runtime errors or unexpected behavior caused by dynamic registry modifications.
 * 
 * 2. **Centralized Configuration:**
 *    - All supported environment types are declared in a single, centralized location.
 *    - Simplifies maintenance and ensures a clear understanding of the available types.
 *
 * 3. **No Runtime Extensions:**
 *    - The factory is not designed to support runtime addition of new environment types.
 *    - Instead, the registry is tightly controlled to reflect only the predefined types,
 *      aligning with the principle of simplicity and minimizing user-facing complexity.
 *
 * **Advantages:**
 * - **Immutable Design:** Prevents runtime registry modifications, ensuring stability and predictability.
 * - **Simple and Maintainable:** Adding new environment types requires modifying only the factory’s 
 *   initialization logic, keeping changes isolated and easy to understand.
 * - **Controlled Extensibility:** New environment types can be added by modifying the `EnvironmentFactory`
 *   directly, avoiding runtime errors from user misconfiguration.
 * - **Static Initialization:** All supported types are initialized once and remain consistent throughout
 *   the program’s lifecycle.
 *
 * **Trade-offs:**
 * - **Less Dynamic:** While this approach is more robust, it sacrifices the ability to register new 
 *   types dynamically (e.g., for plugins or runtime extensions). This trade-off aligns with the goal 
 *   of hiding implementation details from users and maintaining a fixed set of environment types.
 */
object EnvironmentFactory {

    private val registry: Map<String, (Map<String, Any>) -> Environment> = mapOf(
        "conda" to { config ->
            CondaEnvironment(
                name = config["name"] as String,
                dependencies = config.typedList<String>("dependencies"),
                channels = config.typedList<String>("channels").ifEmpty { listOf("defaults") },
                pythonVersion = config["pythonVersion"] as? String
            )
        }
        // TODO: pre-registration of alternative environments
        // Example:
        // register("venv") { config -> VenvEnvironment(...) }
    )

    /**
     * Creates an environment instance based on the type and configuration.
     * @param type The type of the environment (e.g., "conda").
     * @param config The configuration for the environment.
     * @return An instance of the requested environment type.
     */
    fun create(type: String, config: Map<String, Any>): Environment {
        val creator = registry[type]
            ?: throw IllegalArgumentException("Unknown environment type: $type")
        return creator(config)
    }

    private inline fun <reified T> Map<String, Any>.typedList(key: String): List<T> =
    (this[key] as? List<*>)?.filterIsInstance<T>()
        ?: error("Expected `$key` to be a List<${T::class.simpleName}>")
}