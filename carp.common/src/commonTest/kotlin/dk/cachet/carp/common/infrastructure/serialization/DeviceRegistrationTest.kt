package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import kotlin.test.*


/**
 * Tests for [DeviceRegistration] relying on core infrastructure.
 */
class DeviceRegistrationTest
{
    @Test
    fun can_serialize_and_deserialize_device_registration_using_JSON()
    {
        val toSerialize = getTestCases()
        toSerialize.forEach {
            val serialized = testJson.encodeToString( DeviceRegistration.serializer(), it )
            val parsed: DeviceRegistration = testJson.decodeFromString( serialized )
            assertEquals( it, parsed )
        }
    }

    /**
     * Types not known at compile time should not prevent deserializing a device registration, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val toSerialize = getTestCases()
        toSerialize.forEach {
            val unknownRegistration = serializeUnknownDeviceRegistration( it )
            val parsed: DeviceRegistration = testJson.decodeFromString( unknownRegistration )
            assertTrue( parsed is CustomDeviceRegistration )
        }
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val toSerialize = getTestCases()
        toSerialize.forEach {
            val unknownRegistration = serializeUnknownDeviceRegistration( it )
            val parsed: DeviceRegistration = testJson.decodeFromString( unknownRegistration )
            val serialized = testJson.encodeToString( parsed )
            assertEquals( unknownRegistration, serialized )
        }
    }

    private fun getTestCases() = listOf(
        // With null values.
        DefaultDeviceRegistration(),

        // With all nullable fields set.
        DefaultDeviceRegistration(
            deviceDisplayName = "Device name",
            additionalSpecifications = ApplicationData( """{"OS":"Android 42"}""" )
        )
    )

    /**
     * Serialize [registration] as an unknown type using JSON.
     */
    private fun serializeUnknownDeviceRegistration( registration: DeviceRegistration ): String
    {
        var serialized = testJson.encodeToString( DeviceRegistration.serializer(), registration )
        serialized = serialized.makeUnknown( registration )

        return serialized
    }
}
