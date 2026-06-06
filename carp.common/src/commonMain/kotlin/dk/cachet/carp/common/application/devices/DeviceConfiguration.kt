package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.sampling.SamplingConfigurationMapBuilder
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * Describes any type of electronic device, such as a sensor, video camera, desktop computer, or smartphone
 * that collects data which can be incorporated into the platform after it has been processed by a primary device (potentially itself).
 * Optionally, a device can present output and receive user input.
 *
 * TODO: Does this also allow specifying dynamic devices? E.g., 'closest smartphone'. Perhaps a 'DeviceSelector'?
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
@JsExport
abstract class DeviceConfiguration<
    TRegistration : DeviceRegistration,
    out TRegistrationBuilder : DeviceRegistrationBuilder<TRegistration>
>
{
    /**
     * A name which describes how the device participates within the study protocol; it's 'role'.
     * E.g., "Patient's phone"
     */
    abstract val roleName: String

    /**
     * Determines whether device registration for this device is optional prior to starting a study,
     * i.e., whether the study can run without this device or not.
     */
    abstract val isOptional: Boolean

    /**
     * The set of [DataType]s defining which data stream data can be collected on this device.
     */
    abstract fun getSupportedDataTypes(): Set<DataType>

    /**
     * Sampling configurations which override the default configurations for data types available on this device.
     * TODO: Verify whether all configured data types are supported by this device (supported data streams), probably in init.
     *       We might also want to check whether the sampling configuration instances are valid.
     */
    abstract val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>


    /**
     * Do nothing in case [defaultSamplingConfiguration] is valid; throw [IllegalStateException] otherwise.
     * Only known supported data types can be validated; unexpected data types are ignored.
     */
    fun validateDefaultSamplingConfiguration()
    {
        val canBeValidated = getSupportedDataTypes()
        for ( (dataType, samplingConfiguration) in defaultSamplingConfiguration.filter { it.key in canBeValidated } )
        {
            val samplingScheme = checkNotNull( getDataTypeSamplingSchemes()[ dataType ] )
            check( samplingScheme.isValid( samplingConfiguration ) )
                { "The sampling configuration for data type `$dataType` is invalid." }
        }
    }

    /**
     * Return sampling schemes for all the sensors available on this device.
     *
     * Implementations of [DeviceConfiguration] should simply return the mandatory inner object
     * `object Sensors : DataTypeSamplingSchemeMap()` here.
     */
    abstract fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap

    protected abstract fun createDeviceRegistrationBuilder(): TRegistrationBuilder

    /**
     * Create a [DeviceRegistration] which can be used to configure this device for deployment.
     * Use [builder] to configure device-specific registration options, if any.
     */
    fun createRegistration( builder: TRegistrationBuilder.() -> Unit = {} ): TRegistration =
        createDeviceRegistrationBuilder().apply( builder ).build()

    /**
     * Return the class information of the [DeviceRegistration] class used to register devices for this [DeviceConfiguration].
     */
    @JsExport.Ignore
    abstract fun getRegistrationClass(): KClass<TRegistration>

    /**
     * Determines whether the device specifications defined in [registration]
     * live up to the minimum specifications defined by the researcher in this [DeviceConfiguration].
     */
    abstract fun isValidRegistration( registration: TRegistration ): Trilean

    /**
     * Verify whether the passed registration is known to be invalid for the given device.
     * In case this is unknown since the device type is not known at runtime, false is returned.
     */
    fun isDefinitelyInvalidRegistration( registration: DeviceRegistration ): Boolean
    {
        // TODO: `getRegistrationClass` is a trivial implementation in extending classes, but could this be enforced by using the type system instead?
        //  On the JVM runtime, `isValidRegistration` throws a `ClassCastException` when the wrong type were to be passed, but not on JS runtime.
        val isValidType = getRegistrationClass().isInstance( registration )

        @Suppress( "UNCHECKED_CAST" )
        val anyDevice = this as DeviceConfiguration<DeviceRegistration, *>

        return !isValidType || ( anyDevice.isValidRegistration( registration ) == Trilean.FALSE )
    }
}

typealias AnyDeviceConfiguration = DeviceConfiguration<*, *>
typealias DeviceType = KClass<out AnyDeviceConfiguration>


/**
 * A helper class to configure and construct immutable [DeviceConfiguration] classes.
 */
@DeviceConfigurationBuilderDsl
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
abstract class DeviceConfigurationBuilder<TSamplingConfigurationMapBuilder : SamplingConfigurationMapBuilder>
{
    private var samplingConfigurationBuilder: TSamplingConfigurationMapBuilder.() -> Unit = { }

    /**
     * Override default sampling configurations for data types available on this device.
     */
    fun defaultSamplingConfiguration( builder: TSamplingConfigurationMapBuilder.() -> Unit )
    {
        samplingConfigurationBuilder = builder
    }

    protected abstract fun createSamplingConfigurationMapBuilder(): TSamplingConfigurationMapBuilder

    fun buildSamplingConfiguration(): Map<DataType, SamplingConfiguration> =
        createSamplingConfigurationMapBuilder().apply( samplingConfigurationBuilder ).build()
}

/**
 * Should be applied to all builders participating in building [DeviceConfiguration]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class DeviceConfigurationBuilderDsl
