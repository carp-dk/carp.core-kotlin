package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.plan.EnvironmentRef

interface EnvironmentHandler
{

    /**
     * Check if this handler supports the given environment reference.
     */
    fun canHandle( environmentRef: EnvironmentRef ): Boolean

    /**
     * Setup the environment.
     *
     * Called once per environment per run.
     * Creates dependencies, installs packages, etc.
     *
     * @return true if setup succeeded, false otherwise
     */
    fun setup( environmentRef: EnvironmentRef ): Boolean

    /**
     * Generate the command to execute in this environment.
     *
     * Takes a raw command and wraps it with environment activation.
     * For Conda: "conda run -n myenv {command}"
     * For Pixi: "pixi run {command}"
     * For System: "{command}"
     *
     * @param environmentRef The environment to execute in
     * @param command The command to execute (e.g., "python script.py arg1 arg2")
     * @return The wrapped command (e.g., "conda run -n myenv python script.py arg1 arg2")
     */
    fun generateExecutionCommand(
        environmentRef: EnvironmentRef,
        command: String
    ): String

    /**
     * Teardown the environment.
     *
     * Called at workflow end based on cleanup policy.
     * Removes resources (directories, packages, etc.).
     *
     * @return true if teardown succeeded, false otherwise
     */
    fun teardown( environmentRef: EnvironmentRef ): Boolean

    /**
     * Validate that the environment is healthy.
     *
     * Checks that:
     * - Environment exists
     * - Dependencies are installed
     * - Environment is accessible
     *
     * @return true if environment is valid, false otherwise
     */
    fun validate( environmentRef: EnvironmentRef ): Boolean
}
