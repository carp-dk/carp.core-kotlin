package dk.cachet.carp.analytics.application.environment

import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition

/**
 * Factory contract to create an [EnvironmentDefinition] from a parsed YAML section.
 * The provided spec is the map for a single environment section (type, name, config, ...).
 */
interface EnvironmentFactory
{
    /** The key in the YAML section that identifies the environment type (e.g., "type"). */
    val typeKey: String

    /**
     * Registered builders keyed by stable typeId (e.g., "conda").
     * Implementations may keep this immutable.
     */
    val registry: Map<String, (spec: Map<String, Any?>) -> EnvironmentDefinition>

    /** Create an environment instance based on the supplied YAML section. */
    fun create( spec: Map<String, Any?> ): EnvironmentDefinition
}

/**
 * Multiplatform default implementation with an immutable registry of environment builders.
 */
class DefaultEnvironmentFactory(
    override val registry: Map<String, (spec: Map<String, Any?>) -> EnvironmentDefinition>,
    override val typeKey: String = "type"
) : EnvironmentFactory
{
    override fun create( spec: Map<String, Any?> ): EnvironmentDefinition
    {
        val typeId = spec[typeKey] as? String
            ?: error("Missing '$typeKey' in environment specification.")
        val builder = registry[typeId]
            ?: error("Unknown environment type: '$typeId'. Registered: ${registry.keys}")
        return builder(spec)
    }
}
