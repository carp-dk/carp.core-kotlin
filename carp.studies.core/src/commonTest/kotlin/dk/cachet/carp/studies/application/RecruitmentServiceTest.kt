@file:Suppress( "DEPRECATION" )

package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [RecruitmentService].
 */
interface RecruitmentServiceTest
{
    /**
     * System under test: the [recruitmentService] and all dependencies to be used in tests.
     */
    data class SUT(
        val recruitmentService: RecruitmentService,
        val studyService: StudyService,
        val eventBus: EventBus
    )

    /**
     * Create the system under test (SUT): the [RecruitmentService] and all dependencies to be used in tests.
     */
    fun createSUT(): SUT


    @Test
    fun adding_and_retrieving_participants_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId

        val emailParticipant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val usernameParticipant = recruitmentService.addParticipant( studyId, Username( "test" ) )

        // Get participants by ID.
        assertEquals( emailParticipant, recruitmentService.getParticipant( studyId, emailParticipant.id ) )
        assertEquals( usernameParticipant, recruitmentService.getParticipant( studyId, usernameParticipant.id ) )

        // Get all participants.
        val studyParticipants = recruitmentService.getParticipants( studyId )
        assertEquals( setOf( emailParticipant, usernameParticipant ), studyParticipants.toSet() )
    }

    @Test
    fun addParticipant_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        val email = EmailAddress( "test@test.com" )
        assertFailsWith<IllegalArgumentException> { recruitmentService.addParticipant( unknownId, email ) }
    }

    @Suppress( "ReplaceAssertBooleanWithAssertEquality" )
    @Test
    fun addParticipant_twice_returns_same_participant() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId

        val email = EmailAddress( "test@test.com" )
        val p1 = recruitmentService.addParticipant( studyId, email )
        val p2 = recruitmentService.addParticipant( studyId, email )
        assertTrue( p1 == p2 )
    }

    @Test
    fun getParticipant_fails_for_unknown_id() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )

        // Unknown study id.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipant( unknownId, unknownId ) }

        // Unknown participant id.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipant( study.studyId, unknownId ) }
    }

    @Test
    fun getParticipants_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipants( unknownId ) }
    }

    @Test
    fun inviteNewParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val groupName = "Test Group"

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ), groupName )
        assertEquals( participant, groupStatus.participants.single() )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants.single()
        assertEquals( participant, participantInGroup )
        assertEquals( groupName, participantGroups.single().name )
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()
        val assignParticipant = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( unknownId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_empty_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { recruitmentService.inviteNewParticipantGroup( studyId, setOf() ) }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val assignParticipant = AssignedParticipantRoles( unknownId, AssignedTo.All )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_for_unknown_participant_roles() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( "Unknown role" ) ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_fails_when_not_all_participant_roles_assigned() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, protocol) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val role = protocol.participantRoles.first().role
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( role ) ) )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        }
    }

    @Test
    fun inviteNewParticipantGroup_with_same_participants_different_name_creates_new_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus1 = recruitmentService.inviteNewParticipantGroup(
            studyId,
            setOf( assignedParticipant ),
            "Group 1"
        )

        // Deploy the same participants in a group with a different name.
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup(
            studyId,
            setOf( assignedParticipant ),
            "Group 2"
        )
        assertNotEquals( groupStatus1, groupStatus2 )
    }

    @Test
    fun inviteNewParticipantGroup_multiple_times_returns_same_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

        // Deploy the same group a second time.
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun inviteNewParticipantGroup_for_previously_stopped_group_returns_new_group() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

        // Stop previous group. A new deployment with the same participants should be a new participant group.
        recruitmentService.stopParticipantGroup( studyId, groupStatus.id )
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertNotEquals( groupStatus, groupStatus2 )
    }

    @Test
    fun inviteNewParticipantGroup_for_multiple_groups_with_same_name_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val sameName = "Same Group"

        val p1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignedParticipantRoles( p1.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP1 ), sameName )

        // Second group with different participants but same group same name.
        val p2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignedParticipantRoles( p2.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP2 ), sameName )

        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        assertNotEquals( participantGroups[0].id, participantGroups[1].id )
        assertEquals( participantGroups[0].name, participantGroups[1].name )
    }

    @Test
    fun inviteNewParticipantGroup_for_same_participant_different_roles_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, protocol) = createLiveStudy( studyService )
        val participant1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val participant2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val role1 = protocol.participantRoles.map { it.role }.first()
        val role2 = protocol.participantRoles.map { it.role }.last()

        // First group: participant1 has role1, participant2 has role2.
        val assignedSpecificRoles1 = AssignedParticipantRoles( participant1.id, AssignedTo.Roles( setOf ( role1 ) ) )
        val assignedSpecificRoles2 = AssignedParticipantRoles( participant2.id, AssignedTo.Roles( setOf ( role2 ) ) )
        val groupStatus1 = recruitmentService.inviteNewParticipantGroup(
            studyId, setOf( assignedSpecificRoles1, assignedSpecificRoles2 )
        )

        // Second group: participant1 has role2, participant2 has role1.
        val assignedSpecificRoles3 = AssignedParticipantRoles( participant1.id, AssignedTo.Roles( setOf ( role2 ) ) )
        val assignedSpecificRoles4 = AssignedParticipantRoles( participant2.id, AssignedTo.Roles( setOf ( role1 ) ) )
        val groupStatus2 = recruitmentService.inviteNewParticipantGroup(
            studyId, setOf( assignedSpecificRoles3, assignedSpecificRoles4 )
        )
        assertNotEquals( groupStatus1, groupStatus2 )
    }

    @Test
    fun createParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val groupName = "Test Group"

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.createParticipantGroup(
            UUID.randomUUID(),
            setOf( assignParticipant ),
            studyId,
            groupName
        )

        assertEquals( participant, groupStatus.participants.single() )
        assertTrue( groupStatus is ParticipantGroupStatus.Staged )
        assertEquals( groupName, groupStatus.name )
    }

    @Test
    fun createParticipantGroup_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()
        val assignParticipant = AssignedParticipantRoles( UUID.randomUUID(), AssignedTo.All )

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), unknownId )
        }
    }

    @Test
    fun createParticipantGroup_fails_for_study_not_ready_for_deployment() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val participant = recruitmentService.addParticipant( study.studyId, Username( "Test" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )

        assertFailsWith<IllegalStateException>
        {
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), study.studyId )
        }
    }

    @Test
    fun createParticipantGroup_fails_for_unknown_participants() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val assignParticipant = AssignedParticipantRoles( unknownId, AssignedTo.All )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), studyId )
        }
    }

    @Test
    fun createParticipantGroup_fails_for_unknown_participant_roles() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles(
            participant.id,
            AssignedTo.Roles( setOf( "Unknown role" ) )
        )
        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), studyId )
        }
    }

    @Test
    fun createParticipantGroup_for_multiple_groups_with_same_name_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val sameName = "Same group"

        val p1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignedParticipantRoles( p1.id, AssignedTo.All )
        recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignedP1 ), studyId, sameName )

        val p2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignedParticipantRoles( p2.id, AssignedTo.All )
        recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignedP2 ), studyId, sameName )

        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        assertNotEquals( participantGroups[ 0 ].id, participantGroups[ 1 ].id )
        assertEquals( participantGroups[ 0 ].name, participantGroups[ 1 ].name )
    }

    @Test
    fun createParticipantGroup_with_existing_groupId_fails() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupId = UUID.randomUUID()
        recruitmentService.createParticipantGroup( groupId, setOf( assignParticipant ), studyId, "Group 1" )

        assertFailsWith<IllegalArgumentException> {
            recruitmentService.createParticipantGroup( groupId, setOf( assignParticipant ), studyId, "Group 2" )
        }
    }

    @Test
    fun inviteParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val groupName = "Test group"
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.createParticipantGroup(
            UUID.randomUUID(),
            setOf( assignParticipant ),
            studyId,
            groupName
        )

        val invitedGroupStatus = recruitmentService.inviteParticipantGroup( groupStatus.id )

        assertEquals( participant, invitedGroupStatus.participants.single() )
        assertTrue { invitedGroupStatus is ParticipantGroupStatus.Invited }
    }

    @Test
    fun inviteParticipantGroup_fails_for_unknown_groupId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { recruitmentService.inviteParticipantGroup( unknownId ) }
    }

    @Test
    fun inviteParticipantGroup_fails_when_not_all_participant_roles_assigned() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, protocol) = createLiveStudy( studyService ) // Contains more than one role.
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val role = protocol.participantRoles.first().role
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.Roles( setOf( role ) ) )
        val status =
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), studyId )
        assertFailsWith<IllegalArgumentException> { recruitmentService.inviteParticipantGroup( status.id ) }
    }

    @Test
    fun inviteParticipantGroup_for_already_deployed_group_fails() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus =
            recruitmentService.createParticipantGroup( UUID.randomUUID(), setOf( assignParticipant ), studyId )
        recruitmentService.inviteParticipantGroup( groupStatus.id )

        // Deploy the same group a second time.
        assertFailsWith<IllegalStateException> { recruitmentService.inviteParticipantGroup( groupStatus.id ) }
    }

    @Test
    fun getParticipantGroupStatusList_returns_multiple_deployments() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val p1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignedParticipantRoles( p1.id, AssignedTo.All )
        val stagedGroupId = UUID.randomUUID()
        recruitmentService.createParticipantGroup( stagedGroupId, setOf( assignedP1 ), studyId )

        val p2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignedParticipantRoles( p2.id, AssignedTo.All )
        val invitedGroupId = UUID.randomUUID()
        recruitmentService.createParticipantGroup( invitedGroupId, setOf( assignedP2 ), studyId )
        recruitmentService.inviteParticipantGroup( invitedGroupId )

        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        assertEquals( 2, participantGroups.size )
        val stagedGroup = participantGroups.single { it.id == stagedGroupId }
        assertTrue( stagedGroup is ParticipantGroupStatus.Staged )
        assertEquals( p1, stagedGroup.participants.single() )
        val invitedGroup = participantGroups.single { it.id == invitedGroupId }
        assertTrue( invitedGroup is ParticipantGroupStatus.Invited )
        assertEquals( p2, invitedGroup.participants.single() )
    }

    @Test
    fun getParticipantGroupStatusLists_fails_for_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipantGroupStatusList( unknownId ) }
    }

    @Test
    fun stopParticipantGroup_succeeds() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val stagedGroup = recruitmentService.createParticipantGroup(
            UUID.randomUUID(),
            setOf( assignParticipant ),
            studyId
        )
        val groupStatus = recruitmentService.inviteParticipantGroup( stagedGroup.id )

        val stoppedGroupStatus = recruitmentService.stopParticipantGroup( studyId, groupStatus.id )
        assertTrue( stoppedGroupStatus is ParticipantGroupStatus.Stopped )
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_studyId() = runTest {
        val (recruitmentService, _) = createSUT()

        assertFailsWith<IllegalArgumentException>
        {
            recruitmentService.stopParticipantGroup( unknownId, UUID.randomUUID() )
        }
    }

    @Test
    fun stopParticipantGroup_fails_with_unknown_groupId() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        assertFailsWith<IllegalArgumentException> { recruitmentService.stopParticipantGroup( studyId, unknownId ) }
    }

    @Test
    fun stopParticipantGroup_fails_when_group_belongs_to_other_study() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )
        val (otherStudyId, _) = createLiveStudy( studyService )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val stagedGroup = recruitmentService.createParticipantGroup(
            UUID.randomUUID(),
            setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) ),
            studyId
        )
        val groupStatus = recruitmentService.inviteParticipantGroup( stagedGroup.id )

        assertFailsWith<IllegalArgumentException> {
            recruitmentService.stopParticipantGroup( otherStudyId, groupStatus.id )
        }
    }


    private suspend fun createLiveStudy( service: StudyService ): Pair<UUID, StudyProtocolSnapshot>
    {
        // Create deployable protocol.
        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
        protocol.addPrimaryDevice( Smartphone( "User's phone" ) )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addParticipantRole( ParticipantRole( "Test role", false ) )
        protocol.addParticipantRole( ParticipantRole( "Test role 2", false ) )
        protocol.addExpectedParticipantData( expectedData )
        val validSnapshot = protocol.getSnapshot()

        // Create live study from protocol.
        val status = service.createStudy( UUID.randomUUID(), "Test" )
        val studyId = status.studyId
        service.setProtocol( studyId, validSnapshot )
        service.goLive( studyId )

        return Pair( studyId, validSnapshot )
    }
}
