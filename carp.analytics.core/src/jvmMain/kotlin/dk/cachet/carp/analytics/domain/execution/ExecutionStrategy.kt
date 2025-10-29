package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.application.execution.ExecutorFactory
import dk.cachet.carp.analytics.domain.workflow.Workflow
/**
 * Interface for execution strategies.
 * Defines the contract for different strategies to execute processes.
 */
interface ExecutionStrategy
{
    /**
     * Executes the provided processes based on the strategy implementation.
     */
    fun execute( workflow: Workflow, executorFactory: ExecutorFactory )
}
