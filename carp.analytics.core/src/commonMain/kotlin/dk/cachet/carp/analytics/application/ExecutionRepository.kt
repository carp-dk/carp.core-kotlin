package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

/**
 * Repository interface for persisting and retrieving workflow execution data.
 *
 * This includes both state transitions (e.g., QUEUED → RUNNING → COMPLETED) and result summaries or artifacts.
 * Implementations can use any backing store (e.g., SQLite, file system, cloud DB) and should handle consistency guarantees.
 */
interface ExecutionRepository {
    /**
     * Persist a newly created [ExecutionState].
     * @return true if persisted successfully.
     */
    suspend fun saveState(state: ExecutionState): Boolean

    /**
     * Update an existing [ExecutionState] (e.g., to mark as RUNNING, COMPLETED).
     * @return true if the update was applied successfully.
     */
    suspend fun updateState(state: ExecutionState): Boolean
    /**
     * Get the current [ExecutionState] for the given [executionId].
     * @return the state, or null if not found.
     */
    suspend fun getState(executionId: UUID): ExecutionState?
    /**
     * Fetch the latest known status for a given workflow.
     * @return the most recent [ExecutionState] based on start time.
     */
    suspend fun getLatestStatus(workflowId: UUID): ExecutionState?
    /**
     * Search for executions based on study ID and optional filters.
     * @return a list of matching [ExecutionState] records.
     */
    suspend fun findByStudy(
        studyId: UUID,
        workflowId: UUID? = null,
        from: Instant? = null,
        to: Instant? = null
    ): List<ExecutionState>
    /**
     * Save the final result of a completed workflow execution.
     * @return true if saved successfully.
     */
    suspend fun saveResult(result: ExecutionResult): Boolean

    /**
     * Retrieve the result of a previously executed workflow.
     */
    suspend fun getResult(executionId: UUID): ExecutionResult?
}

