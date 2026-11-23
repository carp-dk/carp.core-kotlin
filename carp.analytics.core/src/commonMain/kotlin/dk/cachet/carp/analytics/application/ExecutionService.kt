package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.application.data.ArtifactRecord
import dk.cachet.carp.analytics.application.execution.ExecutionReport
import dk.cachet.carp.analytics.application.execution.ExecutionStatus
import dk.cachet.carp.analytics.application.execution.ExecutorState
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.time.Instant


/**
 * Starts, follows and cancels workflow runs, and returns their results.
 */
interface ExecutionService : ApplicationService<ExecutionService, ExecutionService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event( override val aggregateId: String? ) : IntegrationEvent<ExecutionService>
    {
        constructor( aggregateId: UUID ) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

        @Serializable
        data class RunStarted( val studyId: UUID, val workflowId: UUID, val executionId: UUID ) :
            Event( executionId )

        @Serializable
        data class StepStarted( val executionId: UUID, val stepId: UUID ) :
            Event( executionId )

        @Serializable
        data class StepCompleted( val executionId: UUID, val stepId: UUID, val status: ExecutionStatus ) :
            Event( executionId )

        @Serializable
        data class RunCompleted( val executionId: UUID ) : Event( executionId )

        @Serializable
        data class RunFailed( val executionId: UUID ) : Event( executionId )

        @Serializable
        data class RunCancelled( val executionId: UUID ) : Event( executionId )
    }


    /**
     * Starts a run of the latest version of the stored workflow with [workflowId] in [studyId].
     * Returns straight away; the run continues in the background.
     *
     * @return The new run, or the run already active for this workflow.
     * @throws IllegalArgumentException when no such workflow exists.
     */
    suspend fun executeWorkflow( studyId: UUID, workflowId: UUID ): ExecutorState

    /**
     * Starts a run of [definition], the workflow with its environments, for [studyId] without storing it.
     * Returns straight away; the run continues in the background.
     */
    suspend fun executeWorkflowFromDefinition( studyId: UUID, definition: WorkflowDefinition ): ExecutorState

    /**
     * Stops the run with [executionId]. Steps that already finished keep their outputs.
     *
     * @return The run in its new state, or null when no such run exists in [studyId].
     * A run that already ended is returned unchanged.
     */
    suspend fun cancelExecution( studyId: UUID, executionId: UUID ): ExecutorState?

    /**
     * Returns the current state of the run with [executionId], or null when no such run exists in [studyId].
     */
    suspend fun getExecutionState( studyId: UUID, executionId: UUID ): ExecutorState?

    /**
     * Returns the report of the run with [executionId].
     *
     * @return null when no such run exists in [studyId], or the run has not ended yet.
     */
    suspend fun getExecutionResult( studyId: UUID, executionId: UUID ): ExecutionReport?

    /**
     * Returns runs in [studyId], newest first.
     *
     * @param workflowId Only runs of this workflow, when set.
     * @param from Only runs started at or after this time, when set.
     * @param to Only runs started before this time, when set.
     * @param limit The most runs to return, when set. Use 1 to get the latest run.
     */
    suspend fun findExecutions(
        studyId: UUID,
        workflowId: UUID? = null,
        from: Instant? = null,
        to: Instant? = null,
        limit: Int? = null
    ): List<ExecutorState>

    /**
     * Returns the participant-scoped artefacts produced for the participant in [studyDeploymentId],
     * across all finished runs, newest first.
     *
     * @param since Only artefacts with [ArtifactRecord.producedAt] after this time, when set.
     * Pass the latest `producedAt` already seen to check for new results; an empty list means there are none.
     */
    suspend fun getParticipantResults( studyDeploymentId: UUID, since: Instant? = null ): List<ArtifactRecord>
}
