package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.process.ProcessType
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.ApplicationScriptExternalProcess
import dk.cachet.carp.analytics.domain.process.PythonExternalProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.domain.process.WorkflowProcess
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
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
        ): WorkflowProcess {
            return when (type) {
                ProcessType.COMMAND_LINE -> CommandLineExternalProcess(
                    name = name,
                    executionContext = executionContext,
                    commandTemplate = CommandTemplate(config["commandTemplate"] as String),
                    args = (config["arguments"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
                    description = config["description"] as? String
                )
                ProcessType.APPLICATION_SCRIPT -> ApplicationScriptExternalProcess(
                    name = name,
                    executionContext = executionContext,
                    scriptPath = Path.of(config["scriptPath"] as String),
                    parameters = (config["parameters"] as? Map<*, *>)?.filterKeys { it is String }?.filterValues { it is String }?.mapKeys { it.key as String }?.mapValues { it.value as String }.orEmpty(),
                    description = config["description"] as? String

                )
                ProcessType.PYTHON_SCRIPT -> PythonExternalProcess(
                    name = name,
                    executionContext = executionContext,
                    scriptPath = Path.of(config["scriptPath"] as String).toString(),
                    args = (config["parameters"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
                    description = config["description"] as? String
                )
                ProcessType.DATA_RETRIEVAL -> DataRetrievalProcess(
                    name = name,
                    description = config["description"] as? String ?: "",
                    studyId = UUID(config["studyId"] as String),
                    outputName = config["outputName"] as String,
                    fields = (config["fields"] as? List<*>)?.filterIsInstance<String>()?.toSet(),
                    deviceRoles = (config["deviceRoles"] as? List<*>)?.filterIsInstance<String>()?.toSet(),
                    from = (config["from"] as? String)?.let { Instant.parse(it) },
                    to = (config["to"] as? String)?.let { Instant.parse(it) },
                    offsetDays = (config["offsetDays"] as? Int)
                )
                ProcessType.MEAN_DAILY_STEP_COUNT -> MeanDailyStepCountProcess()
            }
        }
    }
}