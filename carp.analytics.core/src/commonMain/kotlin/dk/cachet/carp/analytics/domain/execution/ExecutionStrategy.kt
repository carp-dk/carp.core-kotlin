package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * Interface for execution strategies.
 * Defines the contract for different strategies to execute processes.
 *
 * Implementations are responsible for managing their own executor factory
 * dependencies as needed for their specific execution approach.
 */
interface ExecutionStrategy
{
    /**
     * Executes the provided workflow based on the strategy implementation.
     *
     * Implementations should handle their own executor factory dependencies
     * internally, allowing for flexible factory management across different
     * deployment scenarios.
     */
    fun execute( workflow: Workflow )
}
