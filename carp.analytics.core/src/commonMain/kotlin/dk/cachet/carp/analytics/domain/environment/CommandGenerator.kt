package dk.cachet.carp.analytics.domain.environment

/**
 * Interface for generating commands for setting up, activating, and tearing down environments.
 */
interface CommandGenerator
{
    fun generateSetupCommand( env: EnvironmentDefinition ): String
    fun generateRunCommand( env: EnvironmentDefinition, command: String ): String
    fun generateActivateCommand( env: EnvironmentDefinition ): String
    fun generateTeardownCommand( env: EnvironmentDefinition ): String
    fun generateListEnvironmentsCommand(): String
    fun generateCreateEnvironmentCommand( env: EnvironmentDefinition ): String
    fun generateInstallDependenciesCommand( env: EnvironmentDefinition ): String?
    fun parseEnvironmentList( output: String ): List<String>
    fun environmentExistsInList( envName: String, availableEnvs: List<String> ): Boolean
    {
        return availableEnvs.contains( envName )
    }
}
