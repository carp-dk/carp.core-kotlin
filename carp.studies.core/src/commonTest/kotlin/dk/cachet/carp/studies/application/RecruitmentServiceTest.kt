package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.*
import dk.cachet.carp.deployments.application.DeploymentService
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
        val deploymentService: DeploymentService,
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

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        assertEquals( participant, groupStatus.participants.single() )
        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        val participantInGroup = participantGroups.single().participants.single()
        assertEquals( participant, participantInGroup )
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
    fun getParticipantGroupStatusList_returns_multiple_deployments() = runTest {
        val (recruitmentService, studyService) = createSUT()
        val (studyId, _) = createLiveStudy( studyService )

        val p1 = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignedP1 = AssignedParticipantRoles( p1.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP1 ) )

        val p2 = recruitmentService.addParticipant( studyId, EmailAddress( "test2@test.com" ) )
        val assignedP2 = AssignedParticipantRoles( p2.id, AssignedTo.All )
        recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignedP2 ) )

        val participantGroups = recruitmentService.getParticipantGroupStatusList( studyId )
        assertEquals( 2, participantGroups.size )
        val deployedParticipants = participantGroups.flatMap { it.participants }.toSet()
        assertEquals( setOf( p1, p2 ), deployedParticipants )
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
        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )

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
    fun getParticipantGroupStatusList_full_flow_succeeds() = runTest {
        val (recruitmentService, studyService, deploymentService) = createSUT()
        val roleName = "User's phone"
        val (studyId, _) = createLiveStudy( studyService, roleName )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        val assignParticipant = AssignedParticipantRoles( participant.id, AssignedTo.All )

        val groupStatus = recruitmentService.inviteNewParticipantGroup( studyId, setOf( assignParticipant ) )
        val deploymentId = groupStatus.id

        val status = deploymentService.getStudyDeploymentStatus( deploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == roleName }
        val registration = primary.createRegistration()

        recruitmentService.getParticipantGroupStatusList( studyId )
        deploymentService.registerDevice( deploymentId, primary.roleName, registration )
        recruitmentService.getParticipantGroupStatusList( studyId )
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( deploymentId, primary.roleName )
        deploymentService.deviceDeployed( deploymentId, primary.roleName, deviceDeployment.lastUpdatedOn )
        recruitmentService.getParticipantGroupStatusList( studyId )
        deploymentService.unregisterDevice( deploymentId, primary.roleName )
        recruitmentService.getParticipantGroupStatusList( studyId )
        recruitmentService.stopParticipantGroup( studyId, deploymentId )
    }

    private suspend fun createLiveStudy(
        service: StudyService,
        roleName: String = "User's phone"
    ): Pair<UUID, StudyProtocolSnapshot>
    {
        // Create deployable protocol.
        val protocol = StudyProtocol( UUID.randomUUID(), "Test protocol" )
        protocol.addPrimaryDevice( Smartphone( roleName ) )
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
