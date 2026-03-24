package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.devices.DefaultDeviceRegistration
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.test.TestClock
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomDeviceConfiguration].
 */
class CustomDeviceConfigurationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_DeviceConfiguration_properties()
    {
        val device = StubDeviceConfiguration( "Unknown" )
        val serialized: String = JSON.encodeToString( StubDeviceConfiguration.serializer(), device )

        val custom = CustomDeviceConfiguration( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectDevice( val incorrect: String = "Not a device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectDevice()
        val serialized: String = JSON.encodeToString( IncorrectDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceConfiguration( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = StubDeviceConfiguration( "Unknown" )
        val serialized: String = JSON.encodeToString( StubDeviceConfiguration.serializer(), device )

        val custom = CustomDeviceConfiguration( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}


/**
 * Tests for [CustomPrimaryDeviceConfiguration].
 */
class CustomPrimaryDeviceConfigurationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_PrimaryDeviceConfiguration_properties()
    {
        val device = StubPrimaryDeviceConfiguration( "Unknown" )
        val serialized: String = JSON.encodeToString( StubPrimaryDeviceConfiguration.serializer(), device )

        val custom = CustomPrimaryDeviceConfiguration( "Irrelevant", serialized, JSON )
        assertEquals( device.roleName, custom.roleName )
    }

    @Serializable
    internal data class IncorrectPrimaryDevice( val incorrect: String = "Not a primary device." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectPrimaryDevice()
        val serialized: String = JSON.encodeToString( IncorrectPrimaryDevice.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomPrimaryDeviceConfiguration( "Irrelevant", serialized, JSON )
        }
    }

    @Test
    fun createRegistration_is_not_supported()
    {
        val device = StubPrimaryDeviceConfiguration( "Unknown" )
        val serialized: String = JSON.encodeToString( StubPrimaryDeviceConfiguration.serializer(), device )

        val custom = CustomPrimaryDeviceConfiguration( "Irrelevant", serialized, JSON )

        assertFailsWith<UnsupportedOperationException>
        {
            custom.createRegistration()
        }
    }
}


/**
 * Tests for [CustomDeviceRegistration].
 */
class CustomDeviceRegistrationTest
{
    companion object
    {
        private val JSON: Json = createDefaultJSON()
    }


    @Test
    fun initialization_from_json_extracts_base_DeviceRegistration_properties()
    {
        val expectedRegistrationCreatedOn = TestClock.now()
        val registration = DefaultDeviceRegistration(
            registrationCreatedOn = expectedRegistrationCreatedOn
        )
        val serialized: String = JSON.encodeToString( DefaultDeviceRegistration.serializer(), registration )

        val custom = CustomDeviceRegistration( "Irrelevant", serialized, JSON )
        assertEquals( registration.deviceId, custom.deviceId )
        assertEquals( registration.deviceDisplayName, custom.deviceDisplayName )
        assertEquals( expectedRegistrationCreatedOn, custom.registrationCreatedOn )
    }

    @Serializable
    internal data class IncorrectRegistration( val incorrect: String = "Not a registration." )

    @Test
    fun initialization_from_invalid_json_fails()
    {
        val incorrect = IncorrectRegistration()
        val serialized: String = JSON.encodeToString( IncorrectRegistration.serializer(), incorrect )

        assertFailsWith<IllegalArgumentException>
        {
            CustomDeviceRegistration( "Irrelevant", serialized, JSON )
        }
    }
}
