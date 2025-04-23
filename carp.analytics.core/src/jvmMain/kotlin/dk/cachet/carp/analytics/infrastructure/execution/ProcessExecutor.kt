package dk.cachet.carp.analytics.infrastructure.execution


import java.io.BufferedReader
import java.io.InputStreamReader


interface ProcessExecutorInterface {
    fun executeCommand(command: String, envVariables: Map<String, String>)
}


/**
 * Utility for executing shell commands with environment variables.
 */
class ProcessExecutor : ProcessExecutorInterface {
    override fun executeCommand(command: String, envVariables: Map<String, String>) {
        try {
            val processBuilder = ProcessBuilder(command.split(" "))
            val environment = processBuilder.environment()
            envVariables.forEach { (key, value) -> environment[key] = value }

            val process = processBuilder.start()

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

