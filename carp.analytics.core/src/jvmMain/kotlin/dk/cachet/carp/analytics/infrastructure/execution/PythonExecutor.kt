package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.domain.process.PythonExternalProcess

/**
 * Executor for Python processes. Handles the execution of Python scripts within a specified environment.
 */
class PythonExecutor(
    private val processExecutor: ProcessExecutorInterface = ProcessExecutor()
) : Executor<PythonExternalProcess> {

    private val environmentSetupExecutor = EnvironmentSetupExecutor()

    override fun setup(process: PythonExternalProcess, context: ExecutionContext) {
        context.environment?.let { env ->
            println("Setting up environment: ${env.name}")
            environmentSetupExecutor.setup(env, context)
        }
    }

    override fun execute(process: PythonExternalProcess, context: ExecutionContext) {
        var command = process.getFormattedCommand()
        val inputBuffer = process.getInputBuffer()

        if (inputBuffer != null) {
            val tempInputPath = writeInputBufferToTempFile(inputBuffer)
            command += " --input \"$tempInputPath\""
        }

        try {
            processExecutor.executeCommand(
                command = command,
                envVariables = context.envVariables
            )
        } catch (e: Exception) {
            println("Error executing Python process: ${e.message}")
            throw e
        }
    }

    private fun writeInputBufferToTempFile(json: String): String {
        val tempFile = kotlin.io.path.createTempFile(prefix = "input_data", suffix = ".json").toFile()
        tempFile.writeText(json)
        tempFile.deleteOnExit() // Optional: auto-delete after JVM exit
        return tempFile.absolutePath
    }

}