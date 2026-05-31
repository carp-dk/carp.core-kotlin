@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.NoOptionsSamplingScheme
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A beacon meeting the open AltBeacon standard.
 */
@Serializable
@JsExport
data class AltBeacon(
    override val roleName: String,
    override val isOptional: Boolean = false,
) : DeviceConfiguration<AltBeaconDeviceRegistration, AltBeaconDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeMap()
    {
        /**
         * The signal strength as measured by the device listening to the [AltBeacon].
         */
        val SIGNAL_STRENGTH = add( NoOptionsSamplingScheme( CarpDataTypes.SIGNAL_STRENGTH ) )
    }

    object Tasks : TaskConfigurationList()

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): AltBeaconDeviceRegistrationBuilder =
        AltBeaconDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<AltBeaconDeviceRegistration> = AltBeaconDeviceRegistration::class
    override fun isValidRegistration( registration: AltBeaconDeviceRegistration ): Trilean = Trilean.TRUE
}


/**
 * A [DeviceRegistration] for [AltBeacon] specifying which beacon to listen to.
 *
 * The beacon ID is 20 bytes, made up out of the recommended subdivision [organizationId], [majorId], and [minorId].
 */
@Serializable
@JsExport
data class AltBeaconDeviceRegistration(
    /**
     * The beacon device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    val manufacturerId: Short,
    /**
     * The first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    val organizationId: UUID,
    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    val majorId: Short,
    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    val minorId: Short,
    /**
     * The average received signal strength at 1 meter from the beacon in decibel-milliwatts (dBm).
     * This value is constrained from -127 to 0.
     */
    val referenceRssi: Short,
    override val deviceDisplayName: String? = null, // TODO: We could map known manufacturerId's to display names.
    override val additionalSpecifications: ApplicationData? = null,
    @Required
    override val registrationCreatedOn: Instant = Clock.System.now()
) : DeviceRegistration()
{
    companion object
    {
        val REFERENCE_RSS_RANGE: IntRange = -127..0
    }
    init
    {
        require( referenceRssi in REFERENCE_RSS_RANGE ) { "Reference RSSI needs to be in the range from -127 to 0." }
    }

    @Required
    override val deviceId: String = "$manufacturerId:$organizationId:$majorId:$minorId"
}


@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( NotSerializable::class )
@JsExport
class AltBeaconDeviceRegistrationBuilder : DeviceRegistrationBuilder<AltBeaconDeviceRegistration>()
{
    /**
     * The beacon's device manufacturer's company identifier code as maintained by the Bluetooth SIG assigned numbers database.
     */
    var manufacturerId: Short = 0x0000

    /**
     * The first 16 bytes of the beacon identifier which should be unique to the advertiser's organizational unit.
     */
    var organizationId: UUID = UUID( "00000000-0000-0000-0000-000000000000" )

    /**
     * The first 2 bytes of the beacon identifier after the [organizationId], commonly named major ID.
     */
    var majorId: Short = 0x0000

    /**
     * The last 2 bytes of the beacon identifier, commonly named minor ID.
     */
    var minorId: Short = 0x0000

    /**
     * The average received signal strength at 1 meter from the beacon in decibel-milliwatts (dBm).
     * This value is constrained from -127 to 0.
     *
     * TODO: This presumes that beacons have a fixed reference RSSI; is this the case?
     */
    var referenceRssi: Short = 0

    override fun build(): AltBeaconDeviceRegistration = AltBeaconDeviceRegistration(
        manufacturerId,
        organizationId,
        majorId,
        minorId,
        referenceRssi,
        deviceDisplayName,
        additionalSpecifications
    )
}
