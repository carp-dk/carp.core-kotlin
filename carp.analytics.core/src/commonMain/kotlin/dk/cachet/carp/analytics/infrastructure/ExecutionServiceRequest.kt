package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.domain.execution.BasicExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.serializer
import kotlin.js.JsExport


@Serializable
@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
sealed class ExecutionServiceRequest<out TReturn> : ApplicationServiceRequest<ExecutionService, TReturn>() {
    @Required
    override val apiVersion: ApiVersion = ExecutionService.API_VERSION

    object Serializer : KSerializer<ExecutionServiceRequest<*>> by ignoreTypeParameters(::serializer)

    @Serializable
    data class ExecuteWorkflow(val studyId: UUID, val workflowId: UUID) : ExecutionServiceRequest<ExecutionState>() {
        override fun getResponseSerializer() = ExecutionState.serializer()
    }

    @Serializable
    data class ExecuteWorkflowFromDefinition(val studyId: UUID, val workflow: Workflow) : ExecutionServiceRequest<ExecutionState>() {
        override fun getResponseSerializer() = ExecutionState.serializer()
    }

    @Serializable
    data class GetExecutionState(val executionId: UUID) : ExecutionServiceRequest<ExecutionState?>() {
        override fun getResponseSerializer() = ExecutionState.serializer().nullable
    }

    @Serializable
    data class GetExecutionResult(val executionId: UUID) : ExecutionServiceRequest<BasicExecutionResult?>() {
        override fun getResponseSerializer() = BasicExecutionResult.serializer().nullable
    }

    @Serializable
    data class FindExecutions(
        val studyId: UUID,
        val workflowId: UUID? = null,
        val from: Instant? = null,
        val to: Instant? = null
    ) : ExecutionServiceRequest<List<ExecutionState>>() {
        override fun getResponseSerializer() = serializer<List<ExecutionState>>()
    }

    @Serializable
    data class GetLatestExecutionStatus(
        val studyId: UUID,
        val workflowId: UUID? = null
    ) : ExecutionServiceRequest<ExecutionState?>() {
        override fun getResponseSerializer() = ExecutionState.serializer().nullable
    }
}
