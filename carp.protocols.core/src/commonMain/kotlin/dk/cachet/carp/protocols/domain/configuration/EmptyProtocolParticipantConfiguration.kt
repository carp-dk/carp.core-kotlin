package dk.cachet.carp.protocols.domain.configuration

import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.application.users.hasNoConflicts
import dk.cachet.carp.common.domain.ExtractUniqueKeyMap


/**
 * An initially empty configuration to start defining expected participants and data to be input by users.
 */
@Suppress( "Immutable", "DataClass" )
class EmptyProtocolParticipantConfiguration : ProtocolParticipantConfiguration
{
    private val _expectedParticipantData: MutableSet<ExpectedParticipantData> = mutableSetOf()

    override val expectedParticipantData: Set<ExpectedParticipantData>
        get() = _expectedParticipantData.toSet()

    private val _participantRoles: ExtractUniqueKeyMap<String, ParticipantRole> =
        ExtractUniqueKeyMap( { role -> role.role } )
        {
            IllegalArgumentException( "Role name \"$it\" is not unique within participant configuration." )
        }

    override val participantRoles: Set<ParticipantRole>
        get() = _participantRoles.values.toSet()


    override fun addExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean
    {
        // Verify whether assigned role names are part of the participant configuration.
        // TODO: can this be made part of `hasNoConflicts`?
        require( isValidAssignment( expectedData.assignedTo ) )
            { "The expected data contains participant role names which aren't part of this participant configuration." }

        try { expectedParticipantData.plus( expectedData ).hasNoConflicts( exceptionOnConflict = true ) }
        catch ( ex: IllegalArgumentException )
        {
            throw IllegalArgumentException( "The expected data conflicts with existing specified existing data.", ex )
        }

        return _expectedParticipantData.add( expectedData )
    }

    override fun removeExpectedParticipantData( expectedData: ExpectedParticipantData ): Boolean =
        _expectedParticipantData.remove( expectedData )

    override fun addParticipantRole( role: ParticipantRole ): Boolean =
        _participantRoles.tryAddIfKeyIsNew( role )
}
