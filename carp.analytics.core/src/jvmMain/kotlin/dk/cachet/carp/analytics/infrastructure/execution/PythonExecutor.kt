package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.domain.process.PythonProcess

/**
 * Executor for Python processes. Handles the execution of Python scripts within a specified environment.
 */
class PythonExecutor(
    private val processExecutor: ProcessExecutorInterface = ProcessExecutor()
) : Executor<PythonProcess> {

    override fun execute(process: PythonProcess, context: ExecutionContext) {
        val command = process.getFormattedCommand()
        
        try {
            processExecutor.executeCommand(command, context.envVariables)
        } catch (e: Exception) {
            // Proper logging should be added here
            println("Error executing Python process: ${e.message}")
            throw e
        }
    }
}
