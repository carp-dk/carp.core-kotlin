@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A concrete [DeviceRegistration] which solely implements the base properties and nothing else.
 *
 * The base class can't be made concrete since this would prevent being able to serialize extending types (constructors may not contain parameters which are not properties).
 */
@Serializable
@JsExport
data class DefaultDeviceRegistration(
    override val deviceDisplayName: String? = null,
    override val additionalSpecifications: ApplicationData? = null,
    @Required
    override val deviceId: String = UUID.randomUUID().toString(),
    @Required
    override val registrationCreatedOn: Instant = Clock.System.now()
) : DeviceRegistration()


/**
 * A default device registration builder which solely involves assigning a display name and unique ID to the device.
 * By default, a unique ID (UUID) is generated.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( with = NotSerializable::class )
@JsExport
class DefaultDeviceRegistrationBuilder : DeviceRegistrationBuilder<DefaultDeviceRegistration>()
{
    /**
     * Override the default assigned UUID which has been set as device ID.
     * Make sure this ID is unique for the type of device you are creating a registration for.
     */
    var deviceId: String = UUID.randomUUID().toString()

    override fun build(): DefaultDeviceRegistration =
        DefaultDeviceRegistration( deviceDisplayName, additionalSpecifications, deviceId )
}
