package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.*


/**
 * Base class with tests for [ProtocolParticipantConfiguration] which can be used to test extending types.
 */
interface ProtocolParticipantConfigurationTest
{
    /**
     * Called for each test to create a participant configuration to run tests on.
     */
    fun createParticipantConfiguration(): ProtocolParticipantConfiguration


    @Test
    fun addParticipantRole_succeeds()
    {
        val configuration = createParticipantConfiguration()

        val role = ParticipantRole( "Participant", false )
        val isAdded = configuration.addParticipantRole( role )

        assertTrue( isAdded )
        assertEquals( 1, configuration.participantRoles.size )
        assertEquals( role, configuration.participantRoles.single() )
    }

    @Test
    fun addParticipantRole_ignores_duplicates()
    {
        val configuration = createParticipantConfiguration()
        val role = ParticipantRole( "Participant", false )
        configuration.addParticipantRole( role )

        val isAdded = configuration.addParticipantRole( role )

        assertFalse( isAdded )
        assertEquals( 1, configuration.participantRoles.size )
    }

    @Test
    fun addExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantConfiguration()

        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val expectedData = ExpectedParticipantData( attribute )
        val isAdded = configuration.addExpectedParticipantData( expectedData )

        assertTrue( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
        assertEquals( expectedData, configuration.expectedParticipantData.single() )
    }

    @Test
    fun addExpectedParticipantData_ignores_duplicates()
    {
        val configuration = createParticipantConfiguration()
        val participantRole = "Patient"
        configuration.addParticipantRole( ParticipantRole( participantRole, false ) )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val inputBy = AssignedTo.Roles( setOf( participantRole ) )
        val expectedData = ExpectedParticipantData( attribute, inputBy )
        configuration.addExpectedParticipantData( expectedData )

        val isAdded = configuration.addExpectedParticipantData( expectedData )

        assertFalse( isAdded )
        assertEquals( 1, configuration.expectedParticipantData.size )
    }

    @Test
    fun addExpectedParticipantData_fails_for_unknown_participant_roles()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type"))
        val inputBy = AssignedTo.Roles( setOf( "Unknown" ) )
        val expectedData = ExpectedParticipantData( attribute, inputBy )

        assertFailsWith<IllegalArgumentException> { configuration.addExpectedParticipantData( expectedData ) }
    }

    @Test
    fun addExpectedParticipantData_fails_for_conflicting_participant_attribute()
    {
        val configuration = createParticipantConfiguration()
        val prompt = "Test"
        val attribute = ParticipantAttribute.CustomParticipantAttribute( Text( prompt ) )
        val expectedData = ExpectedParticipantData( attribute )
        configuration.addExpectedParticipantData( expectedData )

        // The API prevents users from creating conflicting attributes, but they can be created through JSON.
        // Modify "prompt" in previously added attribute, but reuse same `InputDataType`.
        val json = createTestJSON()
        val conflictingJson = json.encodeToString( expectedData ).replace( prompt, "Changed!" )
        val conflictingExpectedData: ExpectedParticipantData = json.decodeFromString( conflictingJson )

        assertFailsWith<IllegalArgumentException>
        {
            configuration.addExpectedParticipantData( conflictingExpectedData )
        }
    }

    @Test
    fun addExpectedParticipantData_fails_when_one_role_has_the_same_input_type_assigned_multiple_times()
    {
        val configuration = createParticipantConfiguration()
        val participantRole = "Test"
        configuration.addParticipantRole( ParticipantRole( participantRole, false ) )
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        configuration.addExpectedParticipantData( ExpectedParticipantData( attribute, AssignedTo.All ) )

        assertFailsWith<IllegalArgumentException>
        {
            configuration.addExpectedParticipantData(
                ExpectedParticipantData( attribute, AssignedTo.Roles( setOf( participantRole ) ) )
            )
        }
    }

    @Test
    fun removeExpectedParticipantData_succeeds()
    {
        val configuration = createParticipantConfiguration()
        val attribute = ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "some", "type" ) )
        val expectedData = ExpectedParticipantData( attribute )
        configuration.addExpectedParticipantData( expectedData )

        val isRemoved = configuration.removeExpectedParticipantData( expectedData )

        assertTrue( isRemoved )
        assertEquals( 0, configuration.expectedParticipantData.size )
    }
}
