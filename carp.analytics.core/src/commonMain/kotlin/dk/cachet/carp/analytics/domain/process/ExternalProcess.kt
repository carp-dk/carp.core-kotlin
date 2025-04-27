package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext

/**
 * Defines the contract for externally executed processes (e.g., Python scripts, CLI tools).
 */
interface ExternalProcess {
    val name: String
    val executionContext: ExecutionContext
    fun getArguments(): Any
}
