@file:Suppress( "MatchingDeclarationName" )

package dk.cachet.carp.common.infrastructure.serialization

import dk.cachet.carp.common.application.data.CompletedTask
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.common.infrastructure.test.makeUnknown
import kotlinx.serialization.json.Json
import kotlin.test.*


/**
 * Tests for [CustomData].
 */
class CustomDataTest
{
    companion object
    {
        private val JSON: Json = createTestJSON()
    }


    @Test
    fun can_deserialize_CompletedTask_with_unknown_data()
    {
        val stubData = StubDataPoint( "Some data" )
        val completed = CompletedTask( "Task", stubData )
        val encoded = JSON.encodeToString( completed )
        val unknownInnerData = encoded.makeUnknown( stubData )

        val parsed: CompletedTask = JSON.decodeFromString( unknownInnerData )
        assertTrue( parsed.taskData is CustomData )
    }
}
