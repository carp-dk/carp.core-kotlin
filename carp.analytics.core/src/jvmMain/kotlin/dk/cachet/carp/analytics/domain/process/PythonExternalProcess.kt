package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.data.InMemoryData
import dk.cachet.carp.analytics.application.output.OutputSerializer
import dk.cachet.carp.analytics.domain.data.InputDataReference
import dk.cachet.carp.analytics.domain.data.OutputDataReference
import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.io.path.Path

/**
 * A process that executes Python scripts.
 * @param name The name of the process.
 * @param executionContext Execution context for the process.
 * @param scriptPath Path to the Python script to execute.
 * @param args Optional arguments for the script.
 */
@Serializable
@SerialName("Python")
class PythonExternalProcess(
    override val name: String,
    override val description: String?,
    override val executionContext: ExecutionContext,
    val scriptPath: String,
    val args: MutableList<String> = mutableListOf()
) : ExternalProcess
{

    companion object
    {
        /**
         * If false, skips the script path existence check.
         * This should be disabled in test contexts.
         */
        var validateScriptPath: Boolean = true
    }

    private val scriptArguments: List<String> = args.also {
        require(it.none { arg -> arg.isBlank() }) { "Arguments cannot contain blank strings." }
    }

    @Transient
    private var inputBuffer: String? = null

    override fun getArguments(): List<String> = scriptArguments

    fun getInputBuffer(): String? = inputBuffer

    init
    {
        if (validateScriptPath) validateScriptPath()
    }
    // validate the script path
    private fun validateScriptPath()
    {
        require(scriptPath.isNotBlank()) { "Script path cannot be empty" }
        require(Path(scriptPath).toFile().exists()) { "Script path does not exist: $scriptPath" }
    }

    /**
     * Generates the command to run the Python script using `conda run`.
     */
    fun getFormattedCommand(): String
    {
        val environmentName = executionContext.environment
            ?.name
            ?: error("Environment must be specified")
        val baseCommand = "conda run -n $environmentName python $scriptPath"
        val formattedArgs = scriptArguments.joinToString(" ")
        return "$baseCommand $formattedArgs"
    }

    fun resolveBindings(
        inputData: List<InputDataReference>?,
        outputData: OutputDataReference?,
        dataRegistry: DataRegistry
    )
    {
        inputData?.forEach { inputRef ->
            val source = inputRef.source
            val path = source.segments.joinToString("\\")
            when (source.scheme)
            {
                "inmem" ->
                {
                    val dataHandle = dataRegistry.resolve(path)
                    if (dataHandle is InMemoryData)
                    {
                        inputBuffer = OutputSerializer.serialize(dataHandle.dataset)
                    }
                    else
                    {
                        require(false) { "In-memory data handle not found: $path" }
                    }
                }
                "file" ->
                {
                    args.add("--input $path")
                }
                else -> throw IllegalArgumentException("Unsupported input source scheme: ${source.scheme}")
            }
        }

        outputData?.let { outputRef ->
            val destination = outputRef.destination
            val path = destination.segments.joinToString("\\")
            args.add("--output $path")
        }
    }
}
