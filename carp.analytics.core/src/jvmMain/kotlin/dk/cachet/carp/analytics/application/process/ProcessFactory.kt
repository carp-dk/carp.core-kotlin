package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.Process
import dk.cachet.carp.analytics.domain.process.ProcessType
import dk.cachet.carp.analytics.domain.process.CommandLineProcess
import dk.cachet.carp.analytics.domain.process.ApplicationScriptProcess
import dk.cachet.carp.analytics.domain.process.PythonProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import java.nio.file.Path

/**
 * Factory for creating process instances based on type and configuration.
 */
class ProcessFactory {
    companion object {
        /**
         * Creates a process instance based on the specified type.
         * @param type Type of process to create.
         * @param name Name of the process.
         * @param executionContext Execution context.
         * @param config Configuration map containing process-specific data.
         * @return A new Process instance.
         */
        fun createProcess(
            type: ProcessType,
            name: String,
            executionContext: ExecutionContext,
            config: Map<String, Any>
        ): Process {
            return when (type) {
                ProcessType.COMMAND_LINE -> CommandLineProcess(
                    name = name,
                    executionContext = executionContext,
                    commandTemplate = CommandTemplate(config["commandTemplate"] as String),
                    args = (config["arguments"] as? List<*>)?.filterIsInstance<String>().orEmpty()
                )
                ProcessType.APPLICATION_SCRIPT -> ApplicationScriptProcess(
                    name = name,
                    executionContext = executionContext,
                    scriptPath = Path.of(config["scriptPath"] as String),
                    parameters = (config["parameters"] as? Map<*, *>)?.filterKeys { it is String }?.filterValues { it is String }?.mapKeys { it.key as String }?.mapValues { it.value as String }.orEmpty()
                )
                ProcessType.PYTHON_SCRIPT -> PythonProcess(
                    name = name,
                    executionContext = executionContext,
                    scriptPath = Path.of(config["scriptPath"] as String).toString(),
                    args = (config["parameters"] as? List<*>)?.filterIsInstance<String>().orEmpty()
                )
            }
        }
    }
}