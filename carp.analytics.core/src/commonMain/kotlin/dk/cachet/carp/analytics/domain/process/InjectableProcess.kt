package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.application.runtime.RuntimeDependencyKey

/**
 * Interface for workflow processes that require runtime injection of shared dependencies.
 *
 * This allows processes to remain decoupled from infrastructure and receive services such as:
 * - [dk.cachet.carp.analytics.application.runtime.RuntimeDependencies.StudyDataService]
 * - [dk.cachet.carp.analytics.application.data.DataRegistry]
 * - Custom analysis helpers
 *
 * Each process should check for required keys at runtime and fail gracefully or document its contract.
 *
 * The injection map must use [RuntimeDependencyKey] to ensure clarity and type safety of each dependency.
 */
interface InjectableProcess {
    /**
     * Injects runtime dependencies needed by the process.
     *
     * @param dependencies A map of dependency keys to instances.
     *                     Callers are responsible for ensuring required keys are included.
     */
    fun inject(dependencies: Map<RuntimeDependencyKey<*>, Any>)
}
