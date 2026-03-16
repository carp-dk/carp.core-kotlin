package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.plan.EnvironmentRef

/**
 * Orchestrates environment provisioning and execution.
 *
 * Routes to handlers, manages registry, applies configuration.
 */
interface EnvironmentOrchestrator
{

    /**
     * Setup an environment if not already setup.
     *
     * Checks registry if reuseExisting=true.
     * Calls handler.setup() if needed.
     * Registers in EnvironmentRegistry.
     *
     * @return true if setup succeeded (or already existed)
     */
    fun setup( environmentRef: EnvironmentRef ): Boolean

    /**
     * Generate execution command for this environment.
     *
     * @return The wrapped command ready for execution
     */
    fun generateExecutionCommand(
        environmentRef: EnvironmentRef,
        command: String
    ): String

    /**
     * Teardown environment based on clean-up policy.
     *
     * Called at workflow end.
     * REUSE: Do nothing
     * CLEAN: Remove data but keep structure
     * PURGE: Delete entirely
     *
     * @return true if teardown succeeded (or skipped)
     */
    fun teardown( environmentRef: EnvironmentRef ): Boolean
}
