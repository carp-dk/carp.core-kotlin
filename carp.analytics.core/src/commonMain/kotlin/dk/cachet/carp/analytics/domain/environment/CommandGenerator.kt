package dk.cachet.carp.analytics.domain.environment

/**
 * Interface for generating commands for setting up, activating, and tearing down environments.
 */
interface CommandGenerator
{
    fun generateSetupCommand( env: Environment ): String
    fun generateRunCommand( env: Environment, command: String ): String
    fun generateActivateCommand( env: Environment ): String
    fun generateTeardownCommand( env: Environment ): String
    fun generateListEnvironmentsCommand(): String
    fun generateCreateEnvironmentCommand(env: Environment): String
    fun generateInstallDependenciesCommand(env: Environment): String?
    fun parseEnvironmentList(output: String): List<String>
    fun environmentExistsInList(envName: String, availableEnvs: List<String>): Boolean {
        return availableEnvs.contains(envName)
    }
}