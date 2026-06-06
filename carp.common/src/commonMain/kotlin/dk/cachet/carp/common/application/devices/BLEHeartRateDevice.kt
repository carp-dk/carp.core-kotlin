package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingScheme
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * A Bluetooth Low Energy (BLE) device which implements a GATT Heart Rate service (https://www.bluetooth.com/specifications/gatt/services/).
 */
@Serializable
@JsExport
data class BLEHeartRateDevice(
    override val roleName: String,
    override val isOptional: Boolean = false
) : DeviceConfiguration<MACAddressDeviceRegistration, MACAddressDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeMap()
    {
        /**
         * The number of heart contractions (beats) per minute (bpm).
         */
        val HEART_RATE = add( NoOptionsSamplingScheme( CarpDataTypes.HEART_RATE ) )

        /**
         * The time interval between two consecutive heartbeats.
         */
        val INTERBEAT_INTERVAL = add( NoOptionsSamplingScheme( CarpDataTypes.INTERBEAT_INTERVAL ) )

        /**
         * Whether the device is making proper skin contact.
         */
        val SENSOR_SKIN_CONTACT = add( NoOptionsSamplingScheme( CarpDataTypes.SENSOR_SKIN_CONTACT ) )
    }

    object Tasks : TaskConfigurationList()


    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): MACAddressDeviceRegistrationBuilder =
        MACAddressDeviceRegistrationBuilder()
    @JsExport.Ignore
    override fun getRegistrationClass(): KClass<MACAddressDeviceRegistration> = MACAddressDeviceRegistration::class
    override fun isValidRegistration( registration: MACAddressDeviceRegistration ): Trilean = Trilean.TRUE
}
