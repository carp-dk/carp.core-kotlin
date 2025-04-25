package dk.cachet.carp.analytics.domain.process

import kotlin.io.path.Path
import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A process that executes Python scripts.
 * @param name The name of the process.
 * @param executionContext Execution context for the process.
 * @param scriptPath Path to the Python script to execute.
 * @param arguments Optional arguments for the script.
 */
@Serializable
@SerialName("Python")
class PythonProcess(
    override val name: String,
    override val executionContext: ExecutionContext,
    val scriptPath: String,
    val args: List<String> = emptyList()
) :Process {
    private val scriptArguments: List<String> = args.also {
        require(it.none { arg -> arg.isBlank() }) { "Arguments cannot contain blank strings." }
    }
    override fun getArguments(): List<String> = scriptArguments

    init {
        validateScriptPath()
    }
    
    // validate the script path
    private fun validateScriptPath() {
        require(scriptPath.isNotBlank()) { "Script path cannot be empty" }
        require(Path(scriptPath).toFile().exists()) { "Script path does not exist: $scriptPath" }
    }

    /**
     * Generates the command to run the Python script using `conda run`.
     */
    fun getFormattedCommand(): String {
        val environmentName = executionContext.environment?.name ?: throw IllegalStateException("Environment must be specified")
        val baseCommand = "conda run -n $environmentName python $scriptPath"
        val formattedArgs = scriptArguments.joinToString(" ") { arg ->
            if (arg.contains(" ")) "\"$arg\"" else arg
        }
        return "$baseCommand $formattedArgs"
    }
}