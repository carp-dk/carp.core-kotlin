package dk.cachet.carp.deployments.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.CustomInput
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import dk.cachet.carp.deployments.application.users.ParticipantData
import kotlin.test.*


/**
 * Tests for [ParticipantData] relying on core infrastructure.
 */
class ParticipantDataTest
{
    @Test
    fun can_serialize_and_deserialize_using_JSON()
    {
        JSON = createTestJSON()

        val commonData = mapOf( InputDataType( "some", "type" ) to CustomInput( "Test" ) )
        val roleData = listOf(
            ParticipantData.RoleData( "Participant", mapOf( CarpInputDataTypes.SEX to Sex.Male ) )
        )
        val participantData = ParticipantData( UUID.randomUUID(), commonData, roleData )

        val serialized: String = JSON.encodeToString( participantData )
        val parsed: ParticipantData = JSON.decodeFromString( serialized )

        assertEquals( participantData, parsed )
    }
}
