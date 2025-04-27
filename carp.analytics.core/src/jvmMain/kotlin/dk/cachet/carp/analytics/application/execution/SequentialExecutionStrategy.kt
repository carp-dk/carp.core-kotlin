package dk.cachet.carp.analytics.application.execution


import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy
import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.data.application.CollectedDataSet

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

        var previousOutput: CollectedDataSet? = null

        for ((index, step) in workflow.getSteps().withIndex()) {
            println("Running step ${index + 1}/${workflow.getSteps().size}: ${step.name}")

            val process = step.process

            when (process) {
                is ExternalProcess -> {
                    val executor = executorFactory.getExecutor(process)

                    try {
                        println("Setting up ExternalProcess: ${process.name}")
                        executor.setup(process, process.executionContext)

                        println("Executing ExternalProcess: ${process.name}")
                        executor.execute(process, process.executionContext)

                    } catch (e: Exception) {
                        println("Error during ExternalProcess execution: ${process.name}")
                        e.printStackTrace()
                        throw e
                    } finally {
                        println("Cleaning up ExternalProcess: ${process.name}")
                        executor.cleanup(process, process.executionContext)
                    }
                }

                is AnalysisProcess -> {
                    try {
                        println("Executing AnalysisProcess: ${process.name}")
                        val inputDataSet: CollectedDataSet = loadInputData(step, previousOutput)
                        previousOutput = process.process(inputDataSet)
                    } catch (e: Exception) {
                        println("Error during AnalysisProcess execution: ${process.name}")
                        e.printStackTrace()
                        throw e
                    }
                }

                else -> throw IllegalArgumentException("Unsupported process type: ${process::class.simpleName}")
            }
        }

        println("Workflow execution completed successfully.")
    }

    private fun loadInputData(
        step: Step,
        previousOutput: CollectedDataSet?
    ): CollectedDataSet {
        // TODO: Resolve inputData references properly using the ExecutionContext
        // For now, just use previous output
        return previousOutput ?: CollectedDataSet(emptyList())
    }
}