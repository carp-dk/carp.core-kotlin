package dk.cachet.carp.analytics.application.runtime

/**
 * Represents a strongly-typed key used to access runtime dependencies from a dependency map.
 *
 * @param T The type of dependency associated with this key.
 * @property name A human-readable id for the dependency (used for debugging/logging).
 */
data class RuntimeDependencyKey<T : Any>( val name: String )
