package dk.cachet.carp.common.application.devices

import dk.cachet.carp.common.application.Trilean
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.sampling.DataTypeSamplingSchemeMap
import dk.cachet.carp.common.application.sampling.SamplingConfiguration
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.common.application.tasks.TaskConfigurationList
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * A primary device which uses a single [CustomProtocolTask] to determine how to run a study on the device.
 */
@Serializable
@JsExport
data class CustomProtocolDevice( override val roleName: String, override val isOptional: Boolean = false ) :
    PrimaryDeviceConfiguration<DefaultDeviceRegistration, DefaultDeviceRegistrationBuilder>()
{
    object Sensors : DataTypeSamplingSchemeMap()
    object Tasks : TaskConfigurationList()


    // Measures and data types are defined in the custom `CustomProtocolTask.studyProtocol` and thus not managed by core.
    override fun getSupportedDataTypes(): Set<DataType> = Sensors.keys
    override fun getDataTypeSamplingSchemes(): DataTypeSamplingSchemeMap = Sensors

    override val defaultSamplingConfiguration: Map<DataType, SamplingConfiguration> = emptyMap()

    override fun createDeviceRegistrationBuilder(): DefaultDeviceRegistrationBuilder =
        DefaultDeviceRegistrationBuilder()
    @JsExport.Ignore
    override fun getRegistrationClass(): KClass<DefaultDeviceRegistration> = DefaultDeviceRegistration::class
    override fun isValidRegistration( registration: DefaultDeviceRegistration ) = Trilean.TRUE
}
