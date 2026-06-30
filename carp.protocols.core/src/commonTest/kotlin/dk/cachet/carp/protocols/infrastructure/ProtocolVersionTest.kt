package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.application.ProtocolVersion
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests for [ProtocolVersion] relying on core infrastructure.
 */
class ProtocolVersionTest
{
    @Test
    fun can_serialize_and_deserialize_protocol_version_using_JSON()
    {
        val version = ProtocolVersion( "Test", Clock.System.now() )

        val serialized: String = JSON.encodeToString( version )
        val parsed: ProtocolVersion = JSON.decodeFromString( serialized )

        assertEquals( version, parsed )
    }
}
