package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.application.environment.CommandGeneratorFactory
import dk.cachet.carp.analytics.infrastructure.execution.ProcessExecutor

/**
 * Handles the setup, activation, and teardown of environments.
 */
class EnvironmentSetupExecutor {

    /**
     * Sets up the environment by generating and executing the appropriate setup command.
     */
    fun setup(environment: Environment, context: ExecutionContext) {
        println("Setting up environment: ${environment.name}")
        val generator = CommandGeneratorFactory.getGenerator(environment)
        val setupCommand = generator.generateSetupCommand(environment)
        ProcessExecutor().executeCommand(setupCommand, context.envVariables)
    }

    /**
     * Activates the environment by generating and executing the activation command.
     */
    fun activate(environment: Environment, context: ExecutionContext) {
        println("Activating environment: ${environment.name}")
        val generator = CommandGeneratorFactory.getGenerator(environment)
        val activateCommand = generator.generateActivateCommand(environment)
        ProcessExecutor().executeCommand(activateCommand, context.envVariables)
    }

    /**
     * Tears down the environment by generating and executing the teardown command.
     */
    fun teardown(environment: Environment, context: ExecutionContext) {
        println("Tearing down environment: ${environment.name}")
        val generator = CommandGeneratorFactory.getGenerator(environment)
        val teardownCommand = generator.generateTeardownCommand(environment)
        ProcessExecutor().executeCommand(teardownCommand, context.envVariables)
    }
}
