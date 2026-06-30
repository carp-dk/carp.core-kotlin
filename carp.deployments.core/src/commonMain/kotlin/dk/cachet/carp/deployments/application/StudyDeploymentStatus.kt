@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.deployments.application.users.ParticipantStatus
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.time.Instant


/**
 * Describes the status of a study deployment: registered devices, last received data, whether consent has been given, etc.
 */
@Serializable
@JsExport
sealed class StudyDeploymentStatus
{
    /**
     * The time when the deployment was created.
     */
    abstract val createdOn: Instant

    abstract val studyDeploymentId: UUID

    /**
     * The list of all devices part of this study deployment and their status.
     */
    abstract val deviceStatusList: List<DeviceDeploymentStatus>

    /**
     * The list of all participants and their status in this study deployment.
     */
    abstract val participantStatusList: List<ParticipantStatus>

    /**
     * The time when the study deployment was ready for the first time (all devices deployed); null otherwise.
     */
    abstract val startedOn: Instant?


    /**
     * Initial study deployment status, indicating the invited participants have not yet acted on the invitation.
     */
    @Serializable
    data class Invited(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val deviceStatusList: List<DeviceDeploymentStatus>,
        override val participantStatusList: List<ParticipantStatus>,
        override val startedOn: Instant?
    ) : StudyDeploymentStatus()

    /**
     * Participants have started registering devices, but remaining primary devices still need to be deployed.
     */
    @Serializable
    data class DeployingDevices(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val deviceStatusList: List<DeviceDeploymentStatus>,
        override val participantStatusList: List<ParticipantStatus>,
        override val startedOn: Instant?
    ) : StudyDeploymentStatus()

    /**
     * All primary devices have been successfully deployed and data collection has started
     * on the time specified by [startedOn].
     */
    @Serializable
    data class Running(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val deviceStatusList: List<DeviceDeploymentStatus>,
        override val participantStatusList: List<ParticipantStatus>,
        override val startedOn: Instant
    ) : StudyDeploymentStatus()

    /**
     * The study deployment has been stopped and no more data should be collected.
     */
    @Serializable
    data class Stopped(
        override val createdOn: Instant,
        override val studyDeploymentId: UUID,
        override val deviceStatusList: List<DeviceDeploymentStatus>,
        override val participantStatusList: List<ParticipantStatus>,
        override val startedOn: Instant?,
        val stoppedOn: Instant
    ) : StudyDeploymentStatus()


    /**
     * Returns all [AnyDeviceConfiguration]'s in [deviceStatusList] which still require registration.
     */
    fun getRemainingDevicesToRegister(): Set<AnyDeviceConfiguration> =
        deviceStatusList.filterIsInstance<DeviceDeploymentStatus.Unregistered>().map { it.device }.toSet()

    /**
     * Returns all [AnyPrimaryDeviceConfiguration] which are ready for deployment and are not deployed with the correct deployment yet.
     */
    fun getRemainingDevicesReadyToDeploy(): Set<AnyPrimaryDeviceConfiguration> =
        deviceStatusList
            .filter { it is DeviceDeploymentStatus.NotDeployed && it.canObtainDeviceDeployment }
            .map { it.device }
            .filterIsInstance<AnyPrimaryDeviceConfiguration>()
            .toSet()

    /**
     * Get the status of a [device] in this study deployment.
     */
    fun getDeviceStatus( device: AnyDeviceConfiguration ): DeviceDeploymentStatus =
        deviceStatusList.firstOrNull { it.device == device }
            ?: throw IllegalArgumentException( "The given device was not found in this study deployment." )

    /**
     * Get the status of a device with the given [deviceRoleName] in this study deployment.
     */
    @JsName( "getDeviceStatusByRoleName" )
    fun getDeviceStatus( deviceRoleName: String ): DeviceDeploymentStatus =
        deviceStatusList.firstOrNull { it.device.roleName == deviceRoleName }
            ?: throw IllegalArgumentException(
                "The device with the given role name was not found in this study deployment."
            )
}
