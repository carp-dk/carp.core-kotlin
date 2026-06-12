package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Serializable application service requests to [DeploymentService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class DeploymentServiceRequest<out TReturn> : ApplicationServiceRequest<DeploymentService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = DeploymentService.API_VERSION

    object Serializer : KSerializer<DeploymentServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class CreateStudyDeployment(
        val id: UUID,
        val protocol: StudyProtocolSnapshot,
        val invitations: List<ParticipantInvitation>,
        val connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }

    @Serializable
    data class RemoveStudyDeployments( val studyDeploymentIds: Set<UUID> ) : DeploymentServiceRequest<Set<UUID>>()
    {
        override fun getResponseSerializer() = serializer<Set<UUID>>()
    }

    @Serializable
    data class GetStudyDeploymentStatus( val studyDeploymentId: UUID ) :
        DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }

    @Serializable
    data class GetStudyDeploymentStatusList( val studyDeploymentIds: Set<UUID> ) :
        DeploymentServiceRequest<List<StudyDeploymentStatus>>()
    {
        override fun getResponseSerializer() = serializer<List<StudyDeploymentStatus>>()
    }

    @Serializable
    data class RegisterDevice(
        val studyDeploymentId: UUID,
        val deviceRoleName: String,
        val registration: DeviceRegistration
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }

    @Serializable
    data class UnregisterDevice( val studyDeploymentId: UUID, val deviceRoleName: String ) :
        DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }

    @Serializable
    data class GetDeviceDeploymentFor( val studyDeploymentId: UUID, val primaryDeviceRoleName: String ) :
        DeploymentServiceRequest<PrimaryDeviceDeployment>()
    {
        override fun getResponseSerializer() = serializer<PrimaryDeviceDeployment>()
    }

    @Serializable
    data class DeviceDeployed(
        val studyDeploymentId: UUID,
        val primaryDeviceRoleName: String,
        val deviceDeploymentLastUpdatedOn: Instant
    ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }

    @Serializable
    data class Stop( val studyDeploymentId: UUID ) : DeploymentServiceRequest<StudyDeploymentStatus>()
    {
        override fun getResponseSerializer() = serializer<StudyDeploymentStatus>()
    }
}
