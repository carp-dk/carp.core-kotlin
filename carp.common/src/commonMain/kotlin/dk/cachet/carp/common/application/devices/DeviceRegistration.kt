@file:Suppress( "NON_EXPORTABLE_TYPE", "UnnecessaryAbstractClass" )

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * A [DeviceRegistration] uniquely identifies a [DeviceConfiguration] once it is deployed in a study,
 * contains the necessary details for the client on how to connect to the device,
 * and stores device specifications which may be relevant to the researcher when interpreting collection data,
 * such as brand/model name and operating system version.
 */
@Serializable
@Polymorphic
@Immutable
@ImplementAsDataClass
@JsExport
abstract class DeviceRegistration
{
    /**
     * An ID for the device, used to disambiguate between devices of the same type, as provided by the device itself.
     * It is up to specific types of devices to guarantee uniqueness across all devices of the same type.
     *
     * TODO: This might be useful for potential optimizations later (e.g., prevent pulling in data from the same source more than once), but for now is ignored.
     */
    @Required
    abstract val deviceId: String

    /**
     * An optional concise textual representation for display purposes describing the key specifications of the device.
     * E.g., device manufacturer, name, and operating system version.
     */
    abstract val deviceDisplayName: String?

    @Required
    abstract val registrationCreatedOn: Instant

    /**
     * Additional device specifications which may be relevant to the researcher when interpreting collected data.
     * E.g., brand/model name, operating system version, or any other relevant information.
     */
    abstract val additionalSpecifications: ApplicationData?
}


/**
 * A helper class to configure and construct immutable [DeviceRegistration] classes.
 *
 * TODO: This and extending classes are never expected to be serialized,
 *       but need to be [Serializable] since they are specified as generic type parameter on [DeviceConfiguration].
 */
@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( NotSerializable::class )
@DeviceRegistrationBuilderDsl
@JsExport
abstract class DeviceRegistrationBuilder<T : DeviceRegistration>
{
    /**
     * An optional concise textual representation for display purposes describing the key specifications of the device.
     * E.g., device manufacturer, name, and operating system version.
     *
     * In case this is not set, the builder may derive a default name based on the other registration properties.
     */
    var deviceDisplayName: String? = null

    /**
     * Additional device specifications which may be relevant to the researcher when interpreting collected data.
     * E.g., brand/model name, operating system version, or any other relevant information.
     */
    var additionalSpecifications: ApplicationData? = null

    /**
     * Build the immutable [DeviceRegistration] using the current configuration of this [DeviceRegistrationBuilder].
     */
    abstract fun build(): T
}

/**
 * Should be applied to all builders participating in building [DeviceRegistration]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class DeviceRegistrationBuilderDsl
