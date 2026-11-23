package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.application.SubmitResult
import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.analytics.domain.validation.ValidationResult
import dk.cachet.carp.analytics.domain.workflow.StepsLock
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Serializable application service requests to [WorkflowService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class WorkflowServiceRequest<out TReturn> :
    ApplicationServiceRequest<WorkflowService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = WorkflowService.API_VERSION

    object Serializer : KSerializer<WorkflowServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class ValidateWorkflow( val studyId: UUID, val workflowYaml: String ) :
        WorkflowServiceRequest<ValidationResult>()
    {
        override fun getResponseSerializer() = serializer<ValidationResult>()
    }

    @Serializable
    data class SubmitWorkflow( val studyId: UUID, val workflowYaml: String ) :
        WorkflowServiceRequest<SubmitResult>()
    {
        override fun getResponseSerializer() = serializer<SubmitResult>()
    }

    @Serializable
    data class CreateWorkflow( val studyId: UUID, val definition: WorkflowDefinition ) :
        WorkflowServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
    }

    @Serializable
    data class UpdateWorkflow( val studyId: UUID, val definition: WorkflowDefinition ) :
        WorkflowServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
    }

    @Serializable
    data class GetWorkflow( val studyId: UUID, val workflowId: UUID, val version: Version? = null ) :
        WorkflowServiceRequest<WorkflowDefinition?>()
    {
        override fun getResponseSerializer() = serializer<WorkflowDefinition?>()
    }

    @Serializable
    data class ListWorkflows( val studyId: UUID ) : WorkflowServiceRequest<List<WorkflowMetadata>>()
    {
        override fun getResponseSerializer() = serializer<List<WorkflowMetadata>>()
    }

    @Serializable
    data class ListWorkflowVersions( val studyId: UUID, val workflowId: UUID ) :
        WorkflowServiceRequest<List<WorkflowMetadata>>()
    {
        override fun getResponseSerializer() = serializer<List<WorkflowMetadata>>()
    }

    @Serializable
    data class GetStepsLock( val studyId: UUID, val workflowId: UUID, val version: Version? = null ) :
        WorkflowServiceRequest<StepsLock?>()
    {
        override fun getResponseSerializer() = serializer<StepsLock?>()
    }

    @Serializable
    data class DeleteWorkflow( val studyId: UUID, val workflowId: UUID ) : WorkflowServiceRequest<Boolean>()
    {
        override fun getResponseSerializer() = serializer<Boolean>()
    }
}
