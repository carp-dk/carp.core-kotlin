package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.application.data.ArtifactRecord
import dk.cachet.carp.analytics.application.execution.ExecutionReport
import dk.cachet.carp.analytics.application.execution.ExecutorState
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.serializer
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Serializable application service requests to [ExecutionService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class ExecutionServiceRequest<out TReturn> : ApplicationServiceRequest<ExecutionService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = ExecutionService.API_VERSION

    object Serializer : KSerializer<ExecutionServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class ExecuteWorkflow( val studyId: UUID, val workflowId: UUID ) : ExecutionServiceRequest<ExecutorState>()
    {
        override fun getResponseSerializer() = ExecutorState.serializer()
    }

    @Serializable
    data class ExecuteWorkflowFromDefinition( val studyId: UUID, val definition: WorkflowDefinition ) :
        ExecutionServiceRequest<ExecutorState>()
    {
        override fun getResponseSerializer() = ExecutorState.serializer()
    }

    @Serializable
    data class CancelExecution( val studyId: UUID, val executionId: UUID ) : ExecutionServiceRequest<ExecutorState?>()
    {
        override fun getResponseSerializer() = ExecutorState.serializer().nullable
    }

    @Serializable
    data class GetExecutionState( val studyId: UUID, val executionId: UUID ) :
        ExecutionServiceRequest<ExecutorState?>()
    {
        override fun getResponseSerializer() = ExecutorState.serializer().nullable
    }

    @Serializable
    data class GetExecutionResult( val studyId: UUID, val executionId: UUID ) :
        ExecutionServiceRequest<ExecutionReport?>()
    {
        override fun getResponseSerializer() = ExecutionReport.serializer().nullable
    }

    @Serializable
    data class FindExecutions(
        val studyId: UUID,
        val workflowId: UUID? = null,
        val from: Instant? = null,
        val to: Instant? = null,
        val limit: Int? = null
    ) : ExecutionServiceRequest<List<ExecutorState>>()
    {
        override fun getResponseSerializer() = serializer<List<ExecutorState>>()
    }

    @Serializable
    data class GetParticipantResults( val studyDeploymentId: UUID, val since: Instant? = null ) :
        ExecutionServiceRequest<List<ArtifactRecord>>()
    {
        override fun getResponseSerializer() = serializer<List<ArtifactRecord>>()
    }
}
