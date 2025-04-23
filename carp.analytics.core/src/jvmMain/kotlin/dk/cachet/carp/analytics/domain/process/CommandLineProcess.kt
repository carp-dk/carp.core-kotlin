package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.common.infrastructure.services.Command


/**
 * A process that executes command-line instructions.
 * @param name The name of the process.
 * @param executionContext Execution context for the process.
 * @param commandTemplate Encapsulated command template.
 * @param arguments Non-empty list of command arguments.
 */
class CommandLineProcess(
    override val name: String,
    override val executionContext: ExecutionContext,
    val commandTemplate: CommandTemplate,
    arguments: List<String>
) : Process {
    val commandArguments: List<String> = arguments.also {
        require(it.isNotEmpty()) { "Command arguments cannot be empty" }
    }

    override fun getArguments(): List<String> = commandArguments

    /**
     * Combines the template and arguments into a single formatted command.
     * @return A fully formatted command string.
     */
    fun getFormattedCommand(): String = commandTemplate.render(commandArguments)
}
