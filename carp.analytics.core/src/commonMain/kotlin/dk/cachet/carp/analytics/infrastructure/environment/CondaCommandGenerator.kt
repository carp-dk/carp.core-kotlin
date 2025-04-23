package dk.cachet.carp.analytics.infrastructure.environment

import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.domain.environment.CommandGenerator

/**
 * Generates commands for managing Conda environments.
 */
class CondaCommandGenerator : CommandGenerator {

    override fun generateSetupCommand(env: Environment): String {
        require(env is CondaEnvironment) { "Environment must be of type CondaEnvironment" }

        val baseCommand = "conda create -n ${env.name}"
        val dependencyCommand = env.dependencies.joinToString(" ")
        val channelCommand = env.channels.joinToString(" ") { "-c $it" }
        val pythonCommand = env.pythonVersion?.let { "python=$it" } ?: ""

        return listOf(baseCommand, dependencyCommand, channelCommand, pythonCommand)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    override fun generateActivateCommand(env: Environment): String {
        require(env is CondaEnvironment) { "Environment must be of type CondaEnvironment" }
        return "conda activate ${env.name}"
    }

    // Should this be deactivation or removal of the environment? 
    override fun generateTeardownCommand(env: Environment): String {
        require(env is CondaEnvironment) { "Environment must be of type CondaEnvironment" }
        return "conda remove --name ${env.name} --all"
    }
}
