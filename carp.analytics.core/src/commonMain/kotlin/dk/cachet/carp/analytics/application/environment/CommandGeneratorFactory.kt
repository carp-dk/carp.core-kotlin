package dk.cachet.carp.analytics.application.environment


import kotlin.reflect.KClass
import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.domain.environment.CommandGenerator

/**
 * Factory for selecting the appropriate CommandGenerator based on the Environment type.
 */
object CommandGeneratorFactory {

    private val registry: MutableMap<KClass<out Environment>, CommandGenerator> = mutableMapOf()

    /**
     * Returns the appropriate CommandGenerator for the given environment.
     * @param environment The environment for which a CommandGenerator is required.
     * @return The appropriate CommandGenerator instance.
     * @throws IllegalArgumentException If no generator is registered for the environment type.
     */
    fun getGenerator(environment: Environment): CommandGenerator {
        return registry[environment::class]
            ?: throw IllegalArgumentException("No CommandGenerator available for environment type: ${environment::class.simpleName}")
    }

    /**
     * Registers a new CommandGenerator for the specified environment type.
     * This method is primarily intended for testing purposes.
     *
     * @param environmentType The class of the environment type.
     * @param generator The CommandGenerator to register.
     */
    fun registerGenerator(environmentType: KClass<out Environment>, generator: CommandGenerator) {
        registry[environmentType] = generator 
    }

    /**
     * Clears all custom generators from the registry, restoring it to the default state.
     * This method is primarily intended for testing purposes.
     */
    fun clearCustomGenerators() {
        registry.clear()
    }
}
