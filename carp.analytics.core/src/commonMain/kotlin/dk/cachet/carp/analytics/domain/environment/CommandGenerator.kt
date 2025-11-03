package dk.cachet.carp.analytics.domain.environment

/**
 * Interface for generating commands for setting up, activating, and tearing down environments.
 */
interface CommandGenerator
{
    fun generateSetupCommand( env: Environment ): String
    fun generateActivateCommand( env: Environment ): String
    fun generateTeardownCommand( env: Environment ): String
}
