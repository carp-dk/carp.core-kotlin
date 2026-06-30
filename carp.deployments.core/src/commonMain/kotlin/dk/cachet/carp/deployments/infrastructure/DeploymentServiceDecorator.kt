package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlin.time.Instant


class DeploymentServiceDecorator(
    service: DeploymentService,
    requestDecorator: (Command<DeploymentServiceRequest<*>>) -> Command<DeploymentServiceRequest<*>>
) : ApplicationServiceDecorator<DeploymentService, DeploymentServiceRequest<*>>(
        service,
        DeploymentServiceInvoker,
        requestDecorator
    ),
    DeploymentService
{
    override suspend fun createStudyDeployment(
        id: UUID,
        protocol: StudyProtocolSnapshot,
        invitations: List<ParticipantInvitation>,
        connectedDevicePreregistrations: Map<String, DeviceRegistration>
    ) = invoke(
        DeploymentServiceRequest.CreateStudyDeployment( id, protocol, invitations, connectedDevicePreregistrations )
    )

    override suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ) =
        invoke( DeploymentServiceRequest.RemoveStudyDeployments( studyDeploymentIds ) )

    override suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ) =
        invoke( DeploymentServiceRequest.GetStudyDeploymentStatus( studyDeploymentId ) )

    override suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ) =
        invoke( DeploymentServiceRequest.GetStudyDeploymentStatusList( studyDeploymentIds ) )

    override suspend fun registerDevice(
        studyDeploymentId: UUID,
        deviceRoleName: String,
        registration: DeviceRegistration
    ) = invoke( DeploymentServiceRequest.RegisterDevice( studyDeploymentId, deviceRoleName, registration ) )

    override suspend fun unregisterDevice(
        studyDeploymentId: UUID,
        deviceRoleName: String
    ) = invoke( DeploymentServiceRequest.UnregisterDevice( studyDeploymentId, deviceRoleName ) )

    override suspend fun getDeviceDeploymentFor(
        studyDeploymentId: UUID,
        primaryDeviceRoleName: String
    ) = invoke( DeploymentServiceRequest.GetDeviceDeploymentFor( studyDeploymentId, primaryDeviceRoleName ) )

    override suspend fun deviceDeployed(
        studyDeploymentId: UUID,
        primaryDeviceRoleName: String,
        deviceDeploymentLastUpdatedOn: Instant
    ) = invoke(
        DeploymentServiceRequest.DeviceDeployed(
            studyDeploymentId,
            primaryDeviceRoleName,
            deviceDeploymentLastUpdatedOn
        )
    )

    override suspend fun stop( studyDeploymentId: UUID ) = invoke( DeploymentServiceRequest.Stop( studyDeploymentId ) )
}


object DeploymentServiceInvoker : ApplicationServiceInvoker<DeploymentService, DeploymentServiceRequest<*>>
{
    override suspend fun DeploymentServiceRequest<*>.invoke( service: DeploymentService ): Any =
        when ( this )
        {
            is DeploymentServiceRequest.CreateStudyDeployment ->
                service.createStudyDeployment( id, protocol, invitations, connectedDevicePreregistrations )
            is DeploymentServiceRequest.RemoveStudyDeployments ->
                service.removeStudyDeployments( studyDeploymentIds )
            is DeploymentServiceRequest.GetStudyDeploymentStatus ->
                service.getStudyDeploymentStatus( studyDeploymentId )
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
                service.getStudyDeploymentStatusList( studyDeploymentIds )
            is DeploymentServiceRequest.RegisterDevice ->
                service.registerDevice( studyDeploymentId, deviceRoleName, registration )
            is DeploymentServiceRequest.UnregisterDevice ->
                service.unregisterDevice( studyDeploymentId, deviceRoleName )
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
                service.getDeviceDeploymentFor( studyDeploymentId, primaryDeviceRoleName )
            is DeploymentServiceRequest.DeviceDeployed ->
                service.deviceDeployed( studyDeploymentId, primaryDeviceRoleName, deviceDeploymentLastUpdatedOn )
            is DeploymentServiceRequest.Stop ->
                service.stop( studyDeploymentId )
        }
}
