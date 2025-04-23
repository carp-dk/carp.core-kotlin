package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy

class ExecutionEngine {

    fun executeWorkflow(workflow: Workflow, strategy: ExecutionStrategy, executorFactory: ExecutorFactory) {
        println("Executing workflow: ${workflow.name}")
        try {
            strategy.execute(workflow, executorFactory)
        } catch (e: Exception) {
            println("Workflow execution failed: ${workflow.name}")
            throw e
        }
    }
}
