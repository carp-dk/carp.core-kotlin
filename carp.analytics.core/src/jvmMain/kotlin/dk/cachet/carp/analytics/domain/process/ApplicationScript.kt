package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import java.nio.file.Path

/**
 * A process that executes scripts or application files.
 * @param name The name of the process.
 * @param executionContext Execution context for the process.
 * @param scriptPath Path to the script file.
 * @param parameters Key-value pairs representing script parameters.
 */
class ApplicationScriptProcess(
    override val name: String,
    override val executionContext: ExecutionContext,
    val scriptPath: Path,
    val parameters: Map<String, String>
) : Process {
    override fun getArguments(): Map<String, String> = parameters
}