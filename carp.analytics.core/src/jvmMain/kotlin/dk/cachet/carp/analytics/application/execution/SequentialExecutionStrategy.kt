package dk.cachet.carp.analytics.application.execution


import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy
import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * Executes workflow steps sequentially.
 */
class SequentialExecutionStrategy : ExecutionStrategy {

    /**
     * Executes the provided steps in the workflow one by one using the given ExecutorFactory.
     *
     * @param workflow The workflow containing the steps to execute.
     * @param executorFactory The factory for creating executors for each process type.
     */
    override fun execute(workflow: Workflow, executorFactory: ExecutorFactory) {
        println("Starting sequential execution of workflow: ${workflow.name}")

        for ((index, step) in workflow.getSteps().withIndex()) {
            val executor = executorFactory.getExecutor(step.externalProcess)

            try {
                println("Setting up step ${index + 1}/${workflow.getSteps().size}: ${step.name}")
                executor.setup(step.externalProcess, step.externalProcess.executionContext) //  setup

                println("Executing step ${index + 1}: ${step.name}")
                executor.execute(step.externalProcess, step.externalProcess.executionContext) //  execute

            } catch (e: Exception) {
                println("Error during execution of step ${index + 1}: ${step.name}")
                e.printStackTrace()
                throw e
            } finally {
                println("Cleaning up step ${index + 1}: ${step.name}")
                executor.cleanup(step.externalProcess, step.externalProcess.executionContext) //  cleanup
            }
        }

        println("Workflow execution completed successfully.")
    }
}