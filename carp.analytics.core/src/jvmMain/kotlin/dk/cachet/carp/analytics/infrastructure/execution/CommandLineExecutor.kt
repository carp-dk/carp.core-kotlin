package dk.cachet.carp.analytics.infrastructure.execution


import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess


/**
 * CommandLine executor implementation.
 * TODO: change the println statements to proper logging.
 */
class CommandLineExecutor(
    private val processExecutor: ProcessExecutorInterface = ProcessExecutor()
) : Executor<CommandLineExternalProcess> {
    override fun execute(process: CommandLineExternalProcess, context: ExecutionContext) {
        val formattedCommand = process.getFormattedCommand()
        processExecutor.executeCommand(formattedCommand, context.envVariables)
    }
}
