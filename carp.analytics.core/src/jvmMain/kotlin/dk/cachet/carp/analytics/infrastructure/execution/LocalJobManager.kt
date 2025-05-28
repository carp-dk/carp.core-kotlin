package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.execution.ExecutorFactory
import dk.cachet.carp.analytics.application.execution.SequentialExecutionStrategy
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencies
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.execution.JobManager
import dk.cachet.carp.analytics.application.execution.WorkflowInjector
import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.domain.execution.BasicExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.analytics.domain.workflow.Workflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * A local in-process implementation of [JobManager] using coroutines.
 * Suitable for development, demos, and unit testing.
 *
 * In production, this could be replaced by a distributed implementation using:
 * - Redis Queue
 * - Kafka consumer workers
 *
 * All infrastructure-specific logic should be encapsulated behind the [JobManager] interface.
 */
class LocalJobManager(
    private val executionService: ExecutionService,
    private val studyDataService: StudyDataService,
    private val executionRepository: SQLiteExecutionRepository
) : JobManager {

    /**
     * Submit a workflow execution job.
     *
     * This method:
     * - Loads the current execution state
     * - Injects runtime dependencies (e.g., StudyDataService, DataRegistry)
     * - Executes the workflow sequentially
     * - Captures outputs and artifacts
     * - Persists result and status (COMPLETED or FAILURE)
     *
     * This runs in a coroutine using the [Dispatchers.Default] context.
     */
    override fun submitWorkflowJob(executionId: UUID, workflow: Workflow) {
        CoroutineScope(Dispatchers.Default).launch {
            val state = executionService.getExecutionState(executionId)

            if (state == null) {
                throw IllegalStateException("Execution with ID $executionId not found.")
            }
            try {
                val dataRegistry = DataRegistry()
                val executionStrategy = SequentialExecutionStrategy(dataRegistry)

                // Prepare dependencies for runtime injection
                val dependencies = mapOf(
                    RuntimeDependencies.StudyDataService to studyDataService,
                    RuntimeDependencies.DataRegistry to dataRegistry
                )

                // Inject runtime dependencies into all workflow steps
                WorkflowInjector.inject(workflow, dependencies)

                // Mark execution as running
                executionRepository.updateState(
                    state.copy(status = ExecutionStatus.RUNNING)
                )


                // Execute all steps in order
                executionStrategy.execute(workflow, ExecutorFactory)

                // Gather result data and artifacts
                val result = BasicExecutionResult(
                    executionId = executionId,
                    status = ExecutionStatus.COMPLETED,
                    outputs = dataRegistry.toExecutionOutputs(),
                    artifacts = dataRegistry.toArtifacts()
                )

                // Save result
                executionRepository.saveResult(result)

                executionRepository.updateState(
                    state.copy(status = ExecutionStatus.COMPLETED)
                )

            } catch (e: Exception) {
                // Handle any errors during execution
                val now = Clock.System.now()

                executionRepository.updateState(
                    state.copy(
                        status = ExecutionStatus.FAILURE,
                        completedAt = now
                    )
                )

                // Print error to standard error stream
                System.err.println("Execution $executionId failed at $now")
                e.printStackTrace(System.err)
            }
        }
    }
}
