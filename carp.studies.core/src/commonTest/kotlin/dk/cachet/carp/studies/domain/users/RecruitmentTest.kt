package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import kotlinx.datetime.Clock
import kotlin.test.*


/**
 * Tests for [Recruitment].
 */
class RecruitmentTest
{
    private val studyId = UUID.randomUUID()
    private val participantEmail = EmailAddress( "test@test.com" )


    @Test
    fun creating_recruitment_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val recruitment = Recruitment( studyId )
        val participant = recruitment.addParticipant( participantEmail )
        val protocol = createEmptyProtocol()
        val invitation = StudyInvitation( "Test", "A study" )
        val roleAssignment = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        recruitment.lockInStudy( protocol.getSnapshot(), invitation )
        recruitment.addParticipantGroup( roleAssignment )

        val snapshot = recruitment.getSnapshot()
        val fromSnapshot = Recruitment.fromSnapshot( snapshot )

        assertEquals( recruitment.studyId, fromSnapshot.studyId )
        assertEquals( recruitment.getStatus(), fromSnapshot.getStatus() )
        assertEquals( recruitment.participants, fromSnapshot.participants )
        assertEquals( recruitment.participantGroups, fromSnapshot.participantGroups )
    }

    @Test
    fun addParticipant_succeeds()
    {
        val recruitment = Recruitment( studyId )

        val participant = recruitment.addParticipant( participantEmail )
        val participantEvents = recruitment.consumeEvents().filterIsInstance<Recruitment.Event.ParticipantAdded>()
        val retrievedParticipant = recruitment.participants

        assertEquals( EmailAccountIdentity( participantEmail ), participant.accountIdentity )
        assertEquals( participant, retrievedParticipant.single() )
        assertEquals( participant, participantEvents.single().participant )
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant()
    {
        val recruitment = Recruitment( studyId )
        val p1 = recruitment.addParticipant( participantEmail )

        val p2 = recruitment.addParticipant( participantEmail )
        val participantEvents = recruitment.consumeEvents().filterIsInstance<Recruitment.Event.ParticipantAdded>()

        assertTrue( p1 == p2 )
        assertEquals( 1, participantEvents.size ) // Event should only be published for first participant.
    }

    @Test
    fun lockInStudy_succeeds()
    {
        val recruitment = Recruitment( studyId )
        assertTrue( recruitment.getStatus() is RecruitmentStatus.AwaitingStudyToGoLive )

        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()
        val invitation = StudyInvitation( "Study", "This study is about ..." )
        recruitment.lockInStudy( protocol, invitation )

        val statusAfter = recruitment.getStatus()
        assertTrue( statusAfter is RecruitmentStatus.ReadyForDeployment )
        assertEquals( protocol, statusAfter.studyProtocol )
        assertEquals( invitation, statusAfter.invitation )
    }

    @Test
    fun lockInStudy_only_allowed_once()
    {
        val recruitment = Recruitment( studyId )
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()
        val invitation = StudyInvitation( "Some study" )
        recruitment.lockInStudy( protocol, invitation )

        assertFailsWith<IllegalStateException> { recruitment.lockInStudy( protocol, invitation ) }
    }

    @Test
    fun addParticipantGroup_succeeds()
    {
        val recruitment = Recruitment( studyId )
        val participant = recruitment.addParticipant( participantEmail )
        val protocol = createEmptyProtocol()
        val groupName = "Test Group"
        recruitment.lockInStudy( protocol.getSnapshot(), StudyInvitation( "Some study" ) )

        assertTrue( recruitment.getStatus() is RecruitmentStatus.ReadyForDeployment )

        val roleAssignment = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        val group = recruitment.addParticipantGroup( roleAssignment, groupName )
        assertEquals( Recruitment.Event.ParticipantGroupAdded( roleAssignment ), recruitment.consumeEvents().last() )
        assertEquals(
            participant.id,
            recruitment.participantGroups[ group.id ]?.participantIds?.singleOrNull()
        )
        assertEquals( groupName, group.name )
    }

    @Test
    fun addParticipantGroup_fails_when_study_protocol_not_locked_in()
    {
        val recruitment = Recruitment( studyId )
        val participant = recruitment.addParticipant( participantEmail )

        assertFalse( recruitment.getStatus() is RecruitmentStatus.ReadyForDeployment )

        val roleAssignment = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        assertFailsWith<IllegalStateException> { recruitment.addParticipantGroup( roleAssignment ) }
        val participationEvents = recruitment.consumeEvents().filterIsInstance<Recruitment.Event.ParticipantGroupAdded>()
        assertEquals( 0, participationEvents.count() )
    }

    @Test
    fun addParticipantGroup_fails_for_unknown_participant_roles()
    {
        val recruitment = Recruitment( studyId )
        val participant = recruitment.addParticipant( participantEmail )
        val protocol = createEmptyProtocol()
        recruitment.lockInStudy( protocol.getSnapshot(), StudyInvitation( "Some study" ) )
        val unknownRole = AssignedTo.Roles( setOf( "Unknown role" ) )
        val unknownRoleAssignment = setOf( AssignedParticipantRoles( participant.id, unknownRole ) )
        assertFailsWith<IllegalArgumentException> { recruitment.addParticipantGroup( unknownRoleAssignment ) }
    }

    @Test
    fun getParticipantGroupStatus_succeeds()
    {
        val recruitment = Recruitment( studyId )
        val participant = recruitment.addParticipant( participantEmail )
        val roleAssignment = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        val protocol = createEmptyProtocol()
        recruitment.lockInStudy( protocol.getSnapshot(), StudyInvitation( "Some study" ) )
        val group = recruitment.addParticipantGroup( roleAssignment )

        val stubDeploymentStatus =
            StudyDeploymentStatus.DeployingDevices( Clock.System.now(), group.id, emptyList(), emptyList(), null )
        val groupStatus = recruitment.getParticipantGroupStatus( stubDeploymentStatus )

        assertEquals( group.id, groupStatus.id )
        assertEquals( setOf( participant ), groupStatus.participants )
    }

    @Test
    fun getParticipantGroupStatus_fails_for_unknown_studyDeploymentId()
    {
        val recruitment = Recruitment( studyId )

        val unknownId = UUID.randomUUID()
        val stubDeploymentStatus =
            StudyDeploymentStatus.DeployingDevices( Clock.System.now(), unknownId, emptyList(), emptyList(), null )
        assertFailsWith<IllegalArgumentException> {
            recruitment.getParticipantGroupStatus( stubDeploymentStatus )
        }
    }
}
