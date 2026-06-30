@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfiguration
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.triggers.TriggerConfiguration
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.hasNoConflicts
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Contains the entire description and configuration for how a single primary device participates in running a study.
 */
@Serializable
@JsExport
data class PrimaryDeviceDeployment(
    /**
     * The configuration for the primary device this deployment is intended for.
     */
    val deviceConfiguration: AnyPrimaryDeviceConfiguration,
    /**
     * Registrations for this primary device.
     */
    val registration: DeviceRegistration,
    /**
     * The devices this device needs to connect to.
     */
    val connectedDevices: Set<AnyDeviceConfiguration> = emptySet(),
    /**
     * Preregistration of connected devices, including information such as connection properties, stored per role name.
     */
    val connectedDeviceRegistrations: Map<String, DeviceRegistration> = emptyMap(),
    /**
     * All tasks which should be able to be executed on this or connected devices.
     */
    val tasks: Set<TaskConfiguration<*>> = emptySet(),
    /**
     * All triggers originating from this device and connected devices, stored per assigned id unique within the study protocol.
     */
    val triggers: Map<Int, TriggerConfiguration<*>> = emptyMap(),
    /**
     * Determines which tasks need to be started or stopped when the conditions defined by [triggers] are met.
     */
    val taskControls: Set<TaskControl> = emptySet(),
    /**
     * Expected data about participants in the deployment to be input by users.
     */
    val expectedParticipantData: Set<ExpectedParticipantData> = emptySet(),
    /**
     * Application-specific data to be stored as part of a study deployment.
     *
     * This can be used by infrastructures or concrete applications which require exchanging additional data
     * between the protocols and clients subsystems, outside of scope or not yet supported by CARP core.
     */
    val applicationData: ApplicationData? = null
)
{
    init { expectedParticipantData.hasNoConflicts( exceptionOnConflict = true ) }


    /**
     * Runtime info of a primary device or connected device (determined by [isConnectedDevice]) in a study deployment.
     */
    data class RuntimeDeviceInfo(
        val configuration: AnyDeviceConfiguration,
        val isConnectedDevice: Boolean,
        /**
         * The matching device [registration] for device [configuration] in case it has been registered; null otherwise.
         */
        val registration: DeviceRegistration?,
        /**
         * The sampling configuration per data type to use for device when no custom sampling configuration is provided
         * by an ongoing measure.
         */
        val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>,
        /**
         * The set of tasks which may be sent to this device over the course of the deployment,
         * or an empty set in case there are none.
         */
        val tasks: Set<TaskConfiguration<*>>
    )


    /**
     * The time when this device deployment was last updated.
     * This corresponds to the most recent device registration as part of this device deployment.
     */
    val lastUpdatedOn: Instant = (connectedDeviceRegistrations.values + registration).maxOf { it.registrationCreatedOn }

    /**
     * Get info on the primary device and each of the devices this device needs to connect to relevant at study runtime.
     */
    fun getRuntimeDeviceInfo(): List<RuntimeDeviceInfo> =
        connectedDevices.map {
            RuntimeDeviceInfo(
                it,
                isConnectedDevice = true,
                connectedDeviceRegistrations[ it.roleName ],
                getDefaultSamplingConfigurations( it ),
                getDeviceTasks( it )
            )
        }
        .plus(
            // Primary device runtime info.
            RuntimeDeviceInfo(
                deviceConfiguration,
                isConnectedDevice = false,
                registration,
                getDefaultSamplingConfigurations( deviceConfiguration ),
                getDeviceTasks( deviceConfiguration )
            )
        )

    private fun getDefaultSamplingConfigurations( device: AnyDeviceConfiguration ): Map<DataType, SamplingConfiguration>
    {
        val samplingSchemes: DataTypeSamplingSchemeMap = device.getDataTypeSamplingSchemes()

        // Include configurations for unexpected data types in `defaultSamplingConfiguration` for which no scheme exists.
        val dataTypes: Set<DataType> = samplingSchemes.keys + device.defaultSamplingConfiguration.keys

        return dataTypes.associateWith { dataType ->
            device.defaultSamplingConfiguration[ dataType ] ?: samplingSchemes[ dataType ]!!.default
        }
    }

    private fun getDeviceTasks( device: AnyDeviceConfiguration ): Set<TaskConfiguration<*>> = taskControls
        .filter { it.destinationDeviceRoleName == device.roleName }
        .map { triggered -> tasks.first { it.name == triggered.taskName } }
        .toSet()
}
