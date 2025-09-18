package dk.cachet.carp.analytics.application.environment

import dk.cachet.carp.analytics.domain.environment.CommandGenerator
import dk.cachet.carp.analytics.domain.environment.Environment

/**
 * Resolves a [CommandGenerator] for a given [Environment].
 * Uses a string typeId to look up the appropriate generator.
 */
interface CommandGeneratorResolver {
    /** Registry mapping environment typeId to generator. */
    val registry: Map<String, CommandGenerator>

    /** Function to derive a typeId for the given [Environment]. */
    val keySelector: (Environment) -> String

    /** Return the appropriate generator or fail with a clear error. */
    fun get(environment: Environment): CommandGenerator
}

/**
 * Default, multiplatform resolver with immutable registry and explicit key selector.
 */
class DefaultCommandGeneratorResolver(
    override val registry: Map<String, CommandGenerator>,
    override val keySelector: (Environment) -> String
) : CommandGeneratorResolver {
    override fun get(environment: Environment): CommandGenerator {
        val key = keySelector(environment)
        return registry[key]
            ?: error("No CommandGenerator available for environment key '$key'. Registered: ${registry.keys}")
    }
}
