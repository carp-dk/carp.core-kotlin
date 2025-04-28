package dk.cachet.carp.analytics.infrastructure.execution


import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter


interface ProcessExecutorInterface {
    fun executeCommand(
        command: String,
        envVariables: Map<String, String>,
        inputBindings: String? = null
    )
}


/**
 * Utility for executing shell commands with environment variables.
 */
class ProcessExecutor : ProcessExecutorInterface {

    override fun executeCommand(
        command: String,
        envVariables: Map<String, String>,
        inputBindings: String?
    ) {
        try {
            val cmdList: List<String> = listOf("cmd.exe", "/c") + command.split(" ")
            val processBuilder = ProcessBuilder(cmdList)
            val environment = processBuilder.environment()
            envVariables.forEach { (key, value) -> environment[key] = value }

            val process = processBuilder.start()

            // Handle input bindings
            if (!inputBindings.isNullOrEmpty()) {
                println("Piping JSON input into process...")
                try {
                    OutputStreamWriter(process.outputStream).use { writer ->
                        writer.write(inputBindings)
                        writer.flush()
                    }
                } catch (e: Exception) {
                    println("Error writing inputBindings to process stdin: ${e.message}")
                    // Ignore, since the process might have closed stdin immediately
                }
            }

            // Capture process output
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }

            val exitCode = process.waitFor()

            println("Command Output: $output")
            if (error.isNotEmpty()) {
                println("Command Error: $error")
            }
            if (exitCode != 0) {
                throw RuntimeException("Command failed with exit code $exitCode")
            }

        } catch (e: Exception) {
            println("Error executing command: $command")
            e.printStackTrace()
            throw e
        }
    }
}
