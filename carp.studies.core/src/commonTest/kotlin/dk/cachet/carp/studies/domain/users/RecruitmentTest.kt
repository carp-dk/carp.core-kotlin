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
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.coroutines.test.runTest
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
    fun updateParticipantGroup_updates_assignments_and_name_for_staged_group()
    {
        val recruitment = createReadyRecruitment()
        val participant1 = recruitment.addParticipant( participantEmail )
        val participant2 = recruitment.addParticipant( EmailAddress( "test2@test.com" ) )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant1.id, AssignedTo.All ) ),
            "Initial name"
        )

        val updatedAssignments = setOf( AssignedParticipantRoles( participant2.id, AssignedTo.All ) )
        recruitment.updateParticipantGroup( group.id, updatedAssignments, "Updated name" )

        assertEquals( "Updated name", group.name )
        assertEquals( updatedAssignments, group.roleAssignments )
    }

    @Test
    fun updateParticipantGroup_allows_name_change_after_deployed()
    {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) ),
            "Initial name"
        )
        group.markAsDeployed()

        recruitment.updateParticipantGroup( group.id, name = "Renamed after deploy" )

        assertEquals( "Renamed after deploy", group.name )
    }

    @Test
    fun updateParticipantGroup_fails_when_assignments_change_after_deployed()
    {
        val recruitment = createReadyRecruitment()
        val participant1 = recruitment.addParticipant( participantEmail )
        val participant2 = recruitment.addParticipant( EmailAddress( "test2@test.com" ) )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant1.id, AssignedTo.All ) )
        )
        group.markAsDeployed()

        val updatedAssignments = setOf( AssignedParticipantRoles( participant2.id, AssignedTo.All ) )
        assertFailsWith<IllegalStateException>
            { recruitment.updateParticipantGroup( group.id, updatedAssignments ) }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_groupId()
    {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val assignments = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException>
            { recruitment.updateParticipantGroup( unknownId, assignments, "Updated name" ) }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_participants()
    {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val initialAssignments = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        val group = recruitment.addParticipantGroup( initialAssignments )

        val unknownId = UUID.randomUUID()
        val updatedAssignments = setOf( AssignedParticipantRoles( unknownId, AssignedTo.All ) )
        assertFailsWith<IllegalArgumentException>
            { recruitment.updateParticipantGroup( group.id, updatedAssignments ) }
    }

    @Test
    fun updateParticipantGroup_fails_for_unknown_participant_roles()
    {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val initialAssignments = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        val group = recruitment.addParticipantGroup( initialAssignments )

        val updatedAssignments = setOf(
            AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( "Unknown role" ) ) )
        )
        assertFailsWith<IllegalArgumentException>
            { recruitment.updateParticipantGroup( group.id, updatedAssignments ) }
    }

    @Test
    fun getParticipantGroupStatus_for_deployed_group_succeeds() = runTest {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) ),
            "Test Group"
        )
        group.markAsDeployed()
        val deploymentStatus =
            StudyDeploymentStatus.DeployingDevices(
                Clock.System.now(),
                group.id,
                emptyList(),
                emptyList(),
                null
            )

        val groupStatus = recruitment.getParticipantGroupStatus( group.id ) { requestedId ->
            assertEquals( group.id, requestedId )
            deploymentStatus
        }

        val expected = ParticipantGroupStatus.Invited(
            group.id,
            setOf( participant ),
            group.roleAssignments,
            deploymentStatus.createdOn,
            deploymentStatus,
            group.name
        )
        assertEquals( expected, groupStatus )
    }

    @Test
    fun getParticipantGroupStatus_for_staged_group_succeeds() = runTest {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        )

        val status = recruitment.getParticipantGroupStatus( group.id ) {
            error( "Should not request deployment status for staged groups." )
        }

        val expected = ParticipantGroupStatus.Staged(
            group.id,
            setOf( participant ),
            group.roleAssignments,
            group.name
        )
        assertEquals( expected, status )
    }

    @Test
    fun getParticipantGroupStatusList_returns_statuses_for_staged_and_deployed_groups() = runTest {
        val recruitment = createReadyRecruitment()
        val participant1 = recruitment.addParticipant( participantEmail )
        val participant2 = recruitment.addParticipant( EmailAddress( "test2@test.com" ) )
        val assignedRoles1 = AssignedParticipantRoles( participant1.id, AssignedTo.All )
        val assignedRoles2 = AssignedParticipantRoles( participant2.id, AssignedTo.All )
        val deployedGroup = recruitment.addParticipantGroup( setOf( assignedRoles1 ), "Deployed Group" )
        val stagedGroup = recruitment.addParticipantGroup( setOf( assignedRoles2 ), "Staged Group" )
        deployedGroup.markAsDeployed()
        val deploymentStatus =
            StudyDeploymentStatus.DeployingDevices(
                Clock.System.now(),
                deployedGroup.id,
                emptyList(),
                emptyList(),
                null
            )

        val statuses = recruitment.getParticipantGroupStatusList(
            setOf( stagedGroup.id, deployedGroup.id )
        ) { ids ->
            assertEquals( setOf( deployedGroup.id ), ids )
            listOf( deploymentStatus )
        }

        val expectedStatuses = setOf(
            ParticipantGroupStatus.Staged(
                stagedGroup.id,
                setOf( participant2 ),
                stagedGroup.roleAssignments,
                stagedGroup.name
            ),
            ParticipantGroupStatus.Invited(
                deployedGroup.id,
                setOf( participant1 ),
                deployedGroup.roleAssignments,
                deploymentStatus.createdOn,
                deploymentStatus,
                deployedGroup.name
            )
        )
        assertEquals( expectedStatuses, statuses.toSet() )
    }

    @Test
    fun getParticipantGroupStatus_fails_for_unknown_groupId() = runTest {
        val recruitment = createReadyRecruitment()

        val unknownId = UUID.randomUUID()
        assertFailsWith<IllegalArgumentException> {
            recruitment.getParticipantGroupStatus( unknownId ) { error( "Should not be called." ) }
        }
    }

    @Test
    fun getParticipantGroupStatusList_fails_when_deployment_status_is_missing() = runTest {
        val recruitment = createReadyRecruitment()
        val participant = recruitment.addParticipant( participantEmail )
        val group = recruitment.addParticipantGroup(
            setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        )
        group.markAsDeployed()

        assertFailsWith<IllegalArgumentException> {
            recruitment.getParticipantGroupStatusList( setOf( group.id ) ) { emptyList() }
        }
    }


    private fun createReadyRecruitment(): Recruitment
    {
        val recruitment = Recruitment( UUID.randomUUID() )
        val protocol = createSinglePrimaryDeviceProtocol().getSnapshot()
        recruitment.lockInStudy( protocol, StudyInvitation( "Study" ) )
        return recruitment
    }
}
