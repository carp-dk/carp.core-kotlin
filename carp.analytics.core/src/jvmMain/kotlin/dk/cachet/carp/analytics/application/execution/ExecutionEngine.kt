package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy
import dk.cachet.carp.analytics.domain.workflow.Workflow

class ExecutionEngine
{

    fun executeWorkflow( workflow: Workflow, strategy: ExecutionStrategy, executorFactory: ExecutorFactory )
    {
        println("Executing workflow: ${workflow.metadata.name}")
        try
        {
            strategy.execute(workflow, executorFactory)
        }
        catch ( e: IllegalArgumentException )
        {
            println("Invalid argument for workflow: ${workflow.metadata.name}")
            throw e
        }
        catch ( e: IllegalStateException )
        {
            println("Illegal state during workflow execution: ${workflow.metadata.name}")
            throw e
        }
    }
}
