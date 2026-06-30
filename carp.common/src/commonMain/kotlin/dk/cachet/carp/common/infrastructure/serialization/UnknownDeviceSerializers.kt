package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistrationBuilder
import dk.cachet.carp.common.application.devices.PrimaryDeviceConfiguration
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass
import kotlin.time.Instant


/**
 * A wrapper used to load extending types from [DeviceConfiguration] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( DeviceConfigurationSerializer::class )
data class CustomDeviceConfiguration internal constructor(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : DeviceConfiguration<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(), UnknownPolymorphicWrapper
{
    override val roleName: String
    override val isOptional: Boolean

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>

    // This information is not serialized. Therefore, the supported types and sampling schemes are unknown.
    override fun getSupportedDataTypes(): Set<DataType> = emptySet()
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap =
        throw UnsupportedOperationException(
            "The concrete type of this device is not known. Therefore, sampling schemes are unknown."
        )

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        roleName = baseMembers.roleName
        isOptional = baseMembers.isOptional
        defaultSamplingConfiguration = baseMembers.defaultSamplingConfiguration
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException(
            "The concrete type of this device is not known. " +
            "Therefore, it is unknown which registration builder is required."
        )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether a given registration is valid.
     */
    override fun isValidRegistration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}


/**
 * A wrapper used to load extending types from [PrimaryDeviceConfiguration] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( PrimaryDeviceConfigurationSerializer::class )
data class CustomPrimaryDeviceConfiguration internal constructor(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : PrimaryDeviceConfiguration<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>(),
    UnknownPolymorphicWrapper
{
    override val roleName: String
    override val isOptional: Boolean

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration>

    // This information is not serialized. Therefore, the supported types and sampling schemes are unknown.
    override fun getSupportedDataTypes(): Set<DataType> = emptySet()
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap =
        throw UnsupportedOperationException(
            "The concrete type of this device is not known. Therefore, sampling schemes are unknown."
        )

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        roleName = baseMembers.roleName
        isOptional = baseMembers.isOptional
        defaultSamplingConfiguration = baseMembers.defaultSamplingConfiguration
    }

    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException(
            "The concrete type of this device is not known. " +
            "Therefore, it is unknown which registration builder is required."
        )

    override fun getRegistrationClass(): KClass<DeviceRegistration> = DeviceRegistration::class

    /**
     * For unknown types, it cannot be determined whether a given registration is valid.
     */
    override fun isValidRegistration( registration: DeviceRegistration ) = Trilean.UNKNOWN
}


@Serializable
private data class BaseMembers(
    override val roleName: String,
    override val isOptional: Boolean = false,
    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()
) : DeviceConfiguration<DeviceRegistration, DeviceRegistrationBuilder<DeviceRegistration>>()
{
    override fun getSupportedDataTypes(): Set<DataType> =
        throw UnsupportedOperationException()
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap =
        throw UnsupportedOperationException()
    override fun createDeviceRegistrationBuilder(): DeviceRegistrationBuilder<DeviceRegistration> =
        throw UnsupportedOperationException()
    override fun getRegistrationClass(): KClass<DeviceRegistration> =
        throw UnsupportedOperationException()
    override fun isValidRegistration( registration: DeviceRegistration ): Trilean =
        throw UnsupportedOperationException()
}


/**
 * Custom serializer for [DeviceConfiguration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceConfiguration].
 */
object DeviceConfigurationSerializer : UnknownPolymorphicSerializer<AnyDeviceConfiguration, AnyDeviceConfiguration>(
    DeviceConfiguration::class,
    DeviceConfiguration::class,
    verifyUnknownPolymorphicWrapper = false
)
{
    override fun createWrapper( className: String, json: String, serializer: Json ): AnyDeviceConfiguration
    {
        val jsonObject = serializer.parseToJsonElement( json ) as JsonObject
        val isPrimaryDevice = jsonObject.containsKey( AnyPrimaryDeviceConfiguration::isPrimaryDevice.name )

        return if ( isPrimaryDevice )
        {
            CustomPrimaryDeviceConfiguration( className, json, serializer )
        }
        else
        {
            CustomDeviceConfiguration( className, json, serializer )
        }
    }
}

/**
 * Custom serializer for [PrimaryDeviceConfiguration] which enables deserializing types that are unknown at runtime, yet extend from [PrimaryDeviceConfiguration].
 */
object PrimaryDeviceConfigurationSerializer : KSerializer<AnyPrimaryDeviceConfiguration>
    by createUnknownPolymorphicSerializer(
        { className, json, serializer -> CustomPrimaryDeviceConfiguration( className, json, serializer ) }
    )


/**
 * A wrapper used to load extending types from [DeviceRegistration] serialized as JSON which are unknown at runtime.
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( DeviceRegistrationSerializer::class )
data class CustomDeviceRegistration internal constructor(
    override val className: String,
    override val jsonSource: String,
    val serializer: Json
) : DeviceRegistration(), UnknownPolymorphicWrapper
{
    @Serializable
    private data class BaseMembers(
        override val deviceId: String,
        override val deviceDisplayName: String? = null,
        override val registrationCreatedOn: Instant,
        override val additionalSpecifications: ApplicationData? = null
    ) : DeviceRegistration()

    override val deviceId: String
    override val deviceDisplayName: String?
    override val registrationCreatedOn: Instant
    override val additionalSpecifications: ApplicationData?

    init
    {
        val json = Json( serializer ) { ignoreUnknownKeys = true }
        val baseMembers = json.decodeFromString( BaseMembers.serializer(), jsonSource )
        deviceId = baseMembers.deviceId
        deviceDisplayName = baseMembers.deviceDisplayName
        registrationCreatedOn = baseMembers.registrationCreatedOn
        additionalSpecifications = baseMembers.additionalSpecifications
    }
}

/**
 * Custom serializer for a [DeviceRegistration] which enables deserializing types that are unknown at runtime, yet extend from [DeviceRegistration].
 */
object DeviceRegistrationSerializer : KSerializer<DeviceRegistration> by createUnknownPolymorphicSerializer(
    { className, json, serializer -> CustomDeviceRegistration( className, json, serializer ) }
)
