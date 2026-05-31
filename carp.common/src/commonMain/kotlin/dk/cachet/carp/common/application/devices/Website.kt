@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import dk.cachet.carp.common.infrastructure.serialization.NotSerializable
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A website which participates in a study as a primary device.
 */
@Serializable
@JsExport
data class Website(
    override val roleName: String,
    override val isOptional: Boolean = false
) : PrimaryDeviceConfiguration<WebsiteDeviceRegistration, WebsiteDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeMap()
    object Tasks : TaskConfigurationList()

    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): WebsiteDeviceRegistrationBuilder =
        WebsiteDeviceRegistrationBuilder()
    override fun getRegistrationClass(): KClass<WebsiteDeviceRegistration> = WebsiteDeviceRegistration::class
    override fun isValidRegistration( registration: WebsiteDeviceRegistration ): Trilean = Trilean.TRUE
}


/**
 * A [DeviceRegistration] for a [Website], specifying the [url] where the study runs.
 */
@Serializable
@JsExport
data class WebsiteDeviceRegistration(
    val url: String,
    /**
     * The HTTP User-Agent header of the user agent which made the HTTP request to [url].
     */
    val userAgent: String,
    override val deviceDisplayName: String? = userAgent,
    override val additionalSpecifications: ApplicationData? = null,
    @Required
    override val registrationCreatedOn: Instant = Clock.System.now()
) : DeviceRegistration()
{
    @Required
    override val deviceId: String = url
}


@Suppress( "SERIALIZER_TYPE_INCOMPATIBLE" )
@Serializable( NotSerializable::class )
@JsExport
class WebsiteDeviceRegistrationBuilder : DeviceRegistrationBuilder<WebsiteDeviceRegistration>()
{
    /**
     * The web URL from which the [Website] is accessed.
     */
    var url: String = ""

    /**
     * The HTTP User-Agent header of the user agent which made the HTTP request to [url].
     */
    var userAgent: String = ""

    override fun build(): WebsiteDeviceRegistration =
        WebsiteDeviceRegistration( url, userAgent, deviceDisplayName, additionalSpecifications )
}
