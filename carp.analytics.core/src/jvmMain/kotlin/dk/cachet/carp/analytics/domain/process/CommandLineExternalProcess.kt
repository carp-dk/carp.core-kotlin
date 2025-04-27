package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A process that executes command-line instructions.
 * @param name The name of the process.
 * @param executionContext Execution context for the process.
 * @param commandTemplate Encapsulated command template.
 * @param arguments Non-empty list of command arguments.
 */
@Serializable
@SerialName("CommandLine")
class CommandLineExternalProcess(
    override val name: String,
    override val description: String?,
    override val executionContext: ExecutionContext,
    val commandTemplate: CommandTemplate,
    val args: List<String>
) : ExternalProcess {
    val commandArguments: List<String> = args.also {
        require(it.isNotEmpty()) { "Command arguments cannot be empty" }
    }

    override fun getArguments(): List<String> = commandArguments

    /**
     * Combines the template and arguments into a single formatted command.
     * @return A fully formatted command string.
     */
    fun getFormattedCommand(): String = commandTemplate.render(commandArguments)
}
