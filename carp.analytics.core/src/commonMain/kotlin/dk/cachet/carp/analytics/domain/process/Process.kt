package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext

/**
 * Defines the contract for all processes in the system.
 */
interface Process {
    val name: String
    val executionContext: ExecutionContext
    fun getArguments(): Any
}
