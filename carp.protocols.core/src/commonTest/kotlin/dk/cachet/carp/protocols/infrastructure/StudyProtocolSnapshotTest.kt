package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.devices.PrimaryDeviceConfiguration
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.infrastructure.serialization.CustomDeviceConfiguration
import dk.cachet.carp.common.infrastructure.serialization.CustomPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.serialization.CustomTaskConfiguration
import dk.cachet.carp.common.infrastructure.serialization.CustomTriggerConfiguration
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubSamplingConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTriggerConfiguration
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import dk.cachet.carp.common.test.infrastructure.SnapshotTest
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import kotlinx.serialization.*
import kotlin.test.*


/**
 * Tests for [StudyProtocolSnapshot] relying on core infrastructure.
 */
class StudyProtocolSnapshotTest :
    SnapshotTest<StudyProtocol, StudyProtocolSnapshot>( StudyProtocolSnapshot.serializer() )
{
    override fun createObject() = createComplexProtocol()
    override fun changeSnapshotVersion( toChange: StudyProtocolSnapshot, version: Int ) =
        toChange.copy( version = version )

    /**
     * Types not known at compile time should not prevent deserializing a protocol, but should be loaded through a 'Custom' type wrapper.
     */
    @Test
    fun unknown_types_are_wrapped_when_deserializing()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()

        val parsed: StudyProtocolSnapshot = JSON.decodeFromString( serialized )
        val primaryDevice = parsed.primaryDevices.filterIsInstance<CustomPrimaryDeviceConfiguration>().singleOrNull()
        assertNotNull( primaryDevice )
        assertEquals( 1, primaryDevice.defaultSamplingConfiguration.count() )
        assertEquals( 1, parsed.connectedDevices.filterIsInstance<CustomDeviceConfiguration>().count() )
        assertEquals( 1, parsed.tasks.filterIsInstance<CustomTaskConfiguration>().count() )
        val allMeasures = parsed.tasks.flatMap{ t -> t.measures }
        assertEquals( 2, allMeasures.count() )
        assertEquals( 1, parsed.triggers.count { t -> t.value is CustomTriggerConfiguration } )
    }

    @Test
    fun unknown_connected_primary_device_is_deserialized_as_a_primary_device()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        protocol.addPrimaryDevice( primary )
        val unknownPrimary = StubPrimaryDeviceConfiguration( "Unknown primary" )
        protocol.addConnectedDevice( unknownPrimary, primary )

        // Mimic unknown connected primary device.
        var serialized = JSON.encodeToString( protocol.getSnapshot() )
        serialized = serialized.makeUnknown( unknownPrimary, "Unknown primary" )

        val parsed: StudyProtocolSnapshot = JSON.decodeFromString( serialized )
        assertTrue { parsed.connectedDevices.single() is PrimaryDeviceConfiguration }
    }

    /**
     * Types which were wrapped in a 'Custom' type wrapper upon deserialization should be serialized to their original form (returning the original type, not the wrapper).
     */
    @Test
    fun serializing_unknown_types_removes_the_wrapper()
    {
        val serialized: String = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot: StudyProtocolSnapshot = JSON.decodeFromString( serialized )

        val customSerialized = JSON.encodeToString( snapshot )
        assertEquals( serialized, customSerialized )
    }

    @Test
    fun create_protocol_fromSnapshot_with_custom_extending_types_succeeds()
    {
        val serialized = serializeProtocolSnapshotIncludingUnknownTypes()
        val snapshot: StudyProtocolSnapshot = JSON.decodeFromString( serialized )

        StudyProtocol.fromSnapshot( snapshot )
    }

    /**
     * Creates a study protocol which includes:
     * (1) an unknown primary device with unknown sampling configuration and unknown connected device
     * (2) unknown task with an unknown measure and unknown data type, triggered by an unknown trigger
     * (3) known task with an unknown measure and known data type
     * There is thus exactly one unknown object for each of these types, except for 'Measure' which has two.
     */
    private fun serializeProtocolSnapshotIncludingUnknownTypes(): String
    {
        val protocol = createComplexProtocol()

        // (1) Add unknown primary with unknown sampling configuration and unknown connected device.
        val unknownSamplingConfiguration = StubSamplingConfiguration( "Unknown" )
        val samplingConfiguration = mapOf(
            DataType( "unknown", "type" ) to unknownSamplingConfiguration
        )
        val primary = StubPrimaryDeviceConfiguration( "Unknown", false, samplingConfiguration )
        protocol.addPrimaryDevice( primary )
        val connected = StubDeviceConfiguration( "Unknown 2" )
        protocol.addConnectedDevice( connected, primary )

        // (2) Add unknown task.
        val measures: List<Measure> = listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) )
        val task = StubTaskConfiguration( "Unknown task", measures )
        val trigger = StubTriggerConfiguration( primary.roleName, "Unknown" )
        protocol.addTaskControl( trigger.start( task, primary ) )

        val snapshot: StudyProtocolSnapshot = protocol.getSnapshot()
        var serialized: String = JSON.encodeToString( snapshot )

        // Replace the strings which identify the types to load by the PolymorphicSerializer.
        // This will cause the types not to be found while deserializing, hence mimicking 'custom' types.
        serialized = serialized.makeUnknown( primary, "Unknown" )
        serialized = serialized.makeUnknown( connected )
        serialized = serialized.makeUnknown( unknownSamplingConfiguration, "configuration", "Unknown" )
        serialized = serialized.makeUnknown( task )
        serialized = serialized.makeUnknown( trigger, "uniqueProperty", "Unknown" )

        return serialized
    }
}
