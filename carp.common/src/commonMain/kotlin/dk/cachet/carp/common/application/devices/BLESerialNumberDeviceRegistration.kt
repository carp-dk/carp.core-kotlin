package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A [DeviceRegistration] for Bluetooth Low Energy (BLE) devices which uses the serial number in the
 * Device Information Service GATT spec (https://www.bluetooth.com/specifications/gatt/services/)
 * to uniquely identify the device.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
data class BLESerialNumberDeviceRegistration(
    val serialNumber: String,
    override val deviceDisplayName: String? = null,
    override val additionalSpecifications: ApplicationData? = null,
    @Required
    override val registrationCreatedOn: Instant = Clock.System.now()
) : DeviceRegistration()
{
    init
    {
        require( serialNumber.isNotBlank() )
    }

    @Required
    override val deviceId: String = serialNumber
}


@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( with = NotSerializable::class )
@JsExport
class BLESerialNumberDeviceRegistrationBuilder : DeviceRegistrationBuilder<BLESerialNumberDeviceRegistration>()
{
    /**
     * The serial number, as broadcast by the Device Information GATT service.
     *
     * This should not be blank.
     */
    var serialNumber: String = ""

    override fun build(): BLESerialNumberDeviceRegistration =
        BLESerialNumberDeviceRegistration( serialNumber, deviceDisplayName, additionalSpecifications )
}
