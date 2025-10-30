package dk.cachet.carp.analytics.domain.environment

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