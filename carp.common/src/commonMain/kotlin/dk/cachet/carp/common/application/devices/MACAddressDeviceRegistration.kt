package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.MACAddress
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A [DeviceRegistration] for devices which have a MAC address.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
data class MACAddressDeviceRegistration(
    val macAddress: MACAddress,
    override val deviceDisplayName: String? = null,
    override val additionalSpecifications: ApplicationData? = null,
    @Required
    override val registrationCreatedOn: Instant = Clock.System.now()
) : DeviceRegistration()
{
    @Required
    override val deviceId: String = macAddress.address
}


@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( with = NotSerializable::class )
@JsExport
class MACAddressDeviceRegistrationBuilder : DeviceRegistrationBuilder<MACAddressDeviceRegistration>()
{
    var macAddress: String = ""

    override fun build(): MACAddressDeviceRegistration =
        MACAddressDeviceRegistration( MACAddress.parse( macAddress ), deviceDisplayName, additionalSpecifications )
}
