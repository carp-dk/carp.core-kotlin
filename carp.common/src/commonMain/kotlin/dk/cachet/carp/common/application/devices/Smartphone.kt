package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.*
import dk.cachet.carp.common.application.tasks.*
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds


typealias SmartphoneDeviceRegistration = DefaultDeviceRegistration
typealias SmartphoneDeviceRegistrationBuilder = DefaultDeviceRegistrationBuilder


/**
 * An internet-connected phone with built-in sensors.
 */
@Serializable
@JsExport
data class Smartphone(
    override val roleName: String,
    override val isOptional: Boolean = false,
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()
) : PrimaryDeviceConfiguration<SmartphoneDeviceRegistration, SmartphoneDeviceRegistrationBuilder>()
{
    companion object
    {
        fun create( roleName: String, builder: SmartphoneBuilder.() -> Unit ): Smartphone
        {
            val smartphoneBuilder = SmartphoneBuilder().apply( builder )
            val configuration = smartphoneBuilder.buildSamplingConfiguration()
            return Smartphone( roleName, smartphoneBuilder.isOptional, configuration )
        }
    }

    /**
     * All the sensors commonly available on smartphones.
     */
    @Suppress( "MagicNumber" )
    object Sensors : DataTypeSamplingSchemeMap()
    {
        /**
         *  Geographic location data, representing latitude and longitude within the World Geodetic System 1984.
         */
        val GEOLOCATION = add( AdaptiveGranularitySamplingScheme( CarpDataTypes.GEOLOCATION ) )

        /**
         * Steps within recorded time intervals as reported by a phone's dedicated hardware sensor.
         * Data rate is determined by the sensor.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-stepcounter):
         * - There is a latency of up to 10 s.
         * - Only available starting from Android 4.4.
         *
         * TODO: Android can also 'listen' for steps, which has a delay of about 2 s but is less accurate.
         *       Each 'step' is reported as an event, so this would map to a different DataType (e.g. `Step`).
         *       Not certain this is available on iPhone.
         */
        val STEP_COUNT = add( NoOptionsSamplingScheme( CarpDataTypes.STEP_COUNT ) ) // No options available.

        /**
         * Rate of change in velocity, excluding gravity, along perpendicular x, y, and z axes in the device's coordinate system,
         * as measured by the phone's accelerometer and calibrated to exclude sensor bias.
         * This uses the same coordinate system as the other sensors referring to x, y, and z axes.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_overview):
         * - This corresponds to `TYPE_LINEAR_ACCELERATION`.
         * - The sampling scheme's default measure interval corresponds to `SENSOR_DELAY_NORMAL` (200 ms).
         * - The accelerometer always has an offset, which you need to remove: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
         * - Only available starting from Android 3.0 (API Level 11): earlier versions don't support setting the interval as an absolute value.
         */
        val NON_GRAVITATIONAL_ACCELERATION = add(
            IntervalSamplingScheme( CarpDataTypes.NON_GRAVITATIONAL_ACCELERATION, 200.milliseconds )
        )

        /**
         * Rate of change in velocity, including gravity, along perpendicular x, y, and z axes in the device's coordinate system,
         * as measured by the phone's accelerometer and calibrated to exclude sensor bias.
         * This uses the same coordinate system as the other sensors referring to x, y, and z axes.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_overview):
         * - This corresponds to `TYPE_ACCELEROMETER`.
         * - The sampling scheme's default measure interval corresponds to `SENSOR_DELAY_NORMAL` (200 ms).
         * - The accelerometer always has an offset, which you need to remove: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
         * - Only available starting from Android 3.0 (API Level 11): earlier versions don't support setting the interval as an absolute value.
         */
        val ACCELERATION = add(
            IntervalSamplingScheme( CarpDataTypes.ACCELERATION, 200.milliseconds )
        )

        /**
         * Rate of rotation around perpendicular x, y and z axes,
         * as measured by the phone's gyroscope and calibrated to remove drift and noise.
         * This uses the same coordinate system as the other sensors referring to x, y, and z axes.
         *
         * Android (https://developer.android.com/guide/topics/sensors/sensors_overview):
         * - This corresponds to `TYPE_GYROSCOPE`.
         * - The sampling scheme's default measure interval corresponds to `SENSOR_DELAY_NORMAL` (200 ms).
         * - Only available starting from Android 3.0 (API Level 11): earlier versions don't support setting the interval as an absolute value.
         */
        val ANGULAR_VELOCITY = add(
            IntervalSamplingScheme( CarpDataTypes.ANGULAR_VELOCITY, 200.milliseconds )
        )
    }

    /**
     * ALl tasks commonly available on smartphones.
     */
    @Suppress( "NON_EXPORTABLE_TYPE" )
    object Tasks : TaskConfigurationList()
    {
        /**
         * Redirect to a web page which contains the task which needs to be performed.
         * The passive measures are started when the website is opened and stopped when it is closed.
         */
        val WEB = add { WebTaskBuilder() }
    }

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override fun createDeviceRegistrationBuilder(): SmartphoneDeviceRegistrationBuilder =
        SmartphoneDeviceRegistrationBuilder()
    @JsExport.Ignore
    override fun getRegistrationClass(): KClass<SmartphoneDeviceRegistration> = SmartphoneDeviceRegistration::class
    override fun isValidRegistration( registration: SmartphoneDeviceRegistration ) = Trilean.TRUE
}


/**
 * A helper class to configure and construct immutable [Smartphone] classes.
 */
@JsExport
class SmartphoneBuilder : DeviceConfigurationBuilder<SmartphoneSamplingConfigurationMapBuilder>()
{
    var isOptional: Boolean = false

    override fun createSamplingConfigurationMapBuilder(): SmartphoneSamplingConfigurationMapBuilder =
        SmartphoneSamplingConfigurationMapBuilder()
}


/**
 * A helper class to construct sampling configurations for a [Smartphone].
 */
@JsExport
class SmartphoneSamplingConfigurationMapBuilder : SamplingConfigurationMapBuilder()
{
    /**
     * Configure sampling configuration for [CarpDataTypes.GEOLOCATION].
     */
    fun geolocation( builder: AdaptiveGranularitySamplingConfigurationBuilder.() -> Unit ) =
        addConfiguration( Smartphone.Sensors.GEOLOCATION, builder )

    /**
     * Configure sampling configuration for [CarpDataTypes.NON_GRAVITATIONAL_ACCELERATION].
     */
    fun nonGravitationalAcceleration( builder: IntervalSamplingConfigurationBuilder.() -> Unit ) =
        addConfiguration( Smartphone.Sensors.NON_GRAVITATIONAL_ACCELERATION, builder )

    /**
     * Configure sampling configuration for [CarpDataTypes.ACCELERATION].
     */
    fun acceleration( builder: IntervalSamplingConfigurationBuilder.() -> Unit ) =
        addConfiguration( Smartphone.Sensors.ACCELERATION, builder )

    /**
     * Configure sampling configuration for [CarpDataTypes.ANGULAR_VELOCITY].
     */
    fun angularVelocity( builder: IntervalSamplingConfigurationBuilder.() -> Unit ) =
        addConfiguration( Smartphone.Sensors.ANGULAR_VELOCITY, builder )
}
