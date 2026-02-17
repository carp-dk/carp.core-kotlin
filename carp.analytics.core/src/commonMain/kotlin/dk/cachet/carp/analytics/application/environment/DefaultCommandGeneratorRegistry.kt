package dk.cachet.carp.analytics.application.environment

import dk.cachet.carp.analytics.domain.environment.CommandGenerator
import dk.cachet.carp.analytics.domain.environment.CommandGeneratorResolver
import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition

/**
 * Default, infra-agnostic registry. Ships empty in CORE.
 * Register generator implementations at runtime.
 */
object DefaultCommandGeneratorRegistry : CommandGeneratorResolver
{
    private val _registry: MutableMap<String, CommandGenerator> = mutableMapOf()

    override val registry: Map<String, CommandGenerator> = _registry

    override val keySelector: (EnvironmentDefinition) -> String =
        {
            environment ->
            environment::class.simpleName ?: "Unknown"
        }

    override fun get( environmentDefinition: EnvironmentDefinition ): CommandGenerator
    {
        val key = keySelector(environmentDefinition)
        return registry[key] ?: error("No CommandGenerator for environment type: $key")
    }

    fun register( environmentTypeName: String, generator: CommandGenerator )
    {
        _registry[environmentTypeName] = generator
    }

    fun clear() = _registry.clear()
}
