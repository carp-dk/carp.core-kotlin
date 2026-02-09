package dk.cachet.carp.analytics.application.runtime

interface CommandRunner {
    fun run(command: Command): CommandResult
}