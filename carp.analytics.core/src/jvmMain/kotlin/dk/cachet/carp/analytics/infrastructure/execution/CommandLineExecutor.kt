package dk.cachet.carp.analytics.infrastructure.execution


import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.domain.process.CommandLineProcess

import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * CommandLine executor implementation.
 * TODO: change the println statements to proper logging.
 */
class CommandLineExecutor(
    private val processExecutor: ProcessExecutorInterface = ProcessExecutor()
) : Executor<CommandLineProcess> {
    override fun execute(process: CommandLineProcess, context: ExecutionContext) {
        val formattedCommand = process.getFormattedCommand()
        processExecutor.executeCommand(formattedCommand, context.envVariables)
    }
}
