package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.data.InMemoryData
import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy
import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.data.application.CollectedDataSet

/**
 * Executes workflow steps sequentially using a DataRegistry for data management.
 */
class SequentialExecutionStrategy(
    private val dataRegistry: DataRegistry
) : ExecutionStrategy {
    /**
     * Executes the provided steps in the workflow one by one using the given ExecutorFactory.
     *
     * @param workflow The workflow containing the steps to execute.
     * @param executorFactory The factory for creating executors for each process type.
     */
    override fun execute(workflow: Workflow, executorFactory: ExecutorFactory) {
        println("Starting sequential execution of workflow: ${workflow.name}")

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

                        val inputDataSet: CollectedDataSet = resolveInputData(step)
                        val outputDataSet = process.process(inputDataSet)

                        if (outputDataSet != null && step.outputData != null) {
                            registerOutputData(step, outputDataSet)
                        } else if (outputDataSet == null) {
                            println("AnalysisProcess '${process.name}' produced no output.")
                        }
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

    private fun resolveInputData(step: Step): CollectedDataSet {
        // For now: Assume first inputData is what we fetch
        if (step.inputData.isNullOrEmpty()) {
            println("No input data defined for step '${step.name}', using empty dataset.")
            return CollectedDataSet(emptyList())
        }

        val inputName = step.inputData.first().name
        val handle = dataRegistry.resolve(inputName)

        if (handle is InMemoryData) {
            return handle.dataset
        } else {
            throw IllegalArgumentException("Input data '$inputName' is not in memory or has wrong type.")
        }
    }

    private fun registerOutputData(step: Step, output: CollectedDataSet) {
        if (step.outputData == null) {
            println("No output data reference defined for step '${step.name}', output not stored.")
            return
        }
        val outputName = step.outputData.name
        dataRegistry.register(outputName, InMemoryData(output))
        println("Registered output data under name: '$outputName'")
    }
}