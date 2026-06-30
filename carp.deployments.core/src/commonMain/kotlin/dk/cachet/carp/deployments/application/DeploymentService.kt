package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.*
import kotlin.time.Instant


/**
 * Application service which allows deploying study protocols to participants
 * and retrieving [PrimaryDeviceDeployment]'s for participating primary devices as defined in the protocol.
 */
interface DeploymentService : ApplicationService<DeploymentService, DeploymentService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 3 ) }

    @Serializable
    sealed class Event( override val aggregateId: String? ) : IntegrationEvent<DeploymentService>
    {
        constructor( aggregateId: UUID ) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

        @Serializable
        data class StudyDeploymentCreated(
            val studyDeploymentId: UUID,
            val protocol: StudyProtocolSnapshot,
            val invitations: List<ParticipantInvitation>,
            val connectedDevicePreregistrations: Map<String, DeviceRegistration>
        ) : Event( studyDeploymentId )
        @Serializable
        data class StudyDeploymentRemoved( val studyDeploymentId: UUID ) : Event( studyDeploymentId )
        @Serializable
        data class StudyDeploymentStopped( val studyDeploymentId: UUID ) : Event( studyDeploymentId )
        @Serializable
        data class DeviceRegistrationChanged(
            val studyDeploymentId: UUID,
            val device: AnyDeviceConfiguration,
            val registration: DeviceRegistration?
        ) : Event( studyDeploymentId )
    }


    /**
     * Instantiate a study deployment for a given [StudyProtocolSnapshot] with participants defined in [invitations].
     *
     * The identities specified in the invitations are used to invite and authenticate the participants.
     * In case no account is associated to an identity, a new account is created for it.
     * An invitation (and account details) is delivered to the person managing the identity,
     * or should be handed out manually to the relevant participant by the person managing the identity.
     *
     * @throws IllegalArgumentException when:
     *  - a deployment with [id] already exists
     *  - [protocol] is invalid
     *  - [invitations] is empty
     *  - any of the assigned device roles in [invitations] is not part of the study [protocol]
     *  - not all necessary primary devices part of the study [protocol] have been assigned a participant
     * @return The [StudyDeploymentStatus] of the newly created study deployment.
     */
    suspend fun createStudyDeployment(
        id: UUID,
        protocol: StudyProtocolSnapshot,
        invitations: List<ParticipantInvitation>,
        /**
         * Optional preregistrations for connected devices in the study [protocol].
         */
        connectedDevicePreregistrations: Map<String, DeviceRegistration> = emptyMap()
    ): StudyDeploymentStatus

    /**
     * Remove study deployments with the given [studyDeploymentIds].
     * This also removes all data related to the study deployments managed by [ParticipationService].
     *
     * @return The IDs of study deployments which were removed. IDs for which no study deployment exists are ignored.
     */
    suspend fun removeStudyDeployments( studyDeploymentIds: Set<UUID> ): Set<UUID>

    /**
     * Get the status for a study deployment with the given [studyDeploymentId].
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    suspend fun getStudyDeploymentStatus( studyDeploymentId: UUID ): StudyDeploymentStatus

    /**
     * Get the status for a set of deployments with the specified [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    suspend fun getStudyDeploymentStatusList( studyDeploymentIds: Set<UUID> ): List<StudyDeploymentStatus>

    /**
     * Register the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @param registration A matching configuration for the device with [deviceRoleName].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment or is already registered and a different [registration] is specified than a previous request
     * - [registration] is invalid for the specified device or uses a device ID which has already been used as part of registration of a different device
     * @throws IllegalStateException when this deployment has stopped.
     */
    suspend fun registerDevice(
        studyDeploymentId: UUID,
        deviceRoleName: String,
        registration: DeviceRegistration
    ): StudyDeploymentStatus

    /**
     * Unregister the device with the specified [deviceRoleName] for the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [deviceRoleName] is not present in the deployment
     * @throws IllegalStateException when this deployment has stopped.
     */
    suspend fun unregisterDevice( studyDeploymentId: UUID, deviceRoleName: String ): StudyDeploymentStatus

    /**
     * Get the deployment configuration for the primary device with [primaryDeviceRoleName] in the study deployment with [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [primaryDeviceRoleName] is not present in the deployment
     * - the device with [primaryDeviceRoleName] has not yet been registered
     * @throws IllegalStateException when the deployment for the requested primary device is not yet available.
     */
    suspend fun getDeviceDeploymentFor(
        studyDeploymentId: UUID,
        primaryDeviceRoleName: String
    ): PrimaryDeviceDeployment

    /**
     * Indicate to stakeholders in the study deployment with [studyDeploymentId]
     * that the device with [primaryDeviceRoleName] was deployed successfully,
     * using the device deployment with timestamp [deviceDeploymentLastUpdatedOn],
     * i.e., that the study deployment was loaded on the device and that the necessary runtime is available to run it.
     *
     * @throws IllegalArgumentException when:
     * - a deployment with [studyDeploymentId] does not exist
     * - [primaryDeviceRoleName] is not present in the deployment
     * - the [deviceDeploymentLastUpdatedOn] does not match the expected timestamp. The deployment might be outdated.
     * @throws IllegalStateException when the deployment cannot be deployed yet, or the deployment has stopped.
     */
    suspend fun deviceDeployed(
        studyDeploymentId: UUID,
        primaryDeviceRoleName: String,
        deviceDeploymentLastUpdatedOn: Instant
    ): StudyDeploymentStatus

    /**
     * Stop the study deployment with the specified [studyDeploymentId].
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a deployment with [studyDeploymentId] does not exist.
     */
    suspend fun stop( studyDeploymentId: UUID ): StudyDeploymentStatus
}
