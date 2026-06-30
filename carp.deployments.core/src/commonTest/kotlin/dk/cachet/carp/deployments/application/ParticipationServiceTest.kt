package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createTwoDevicesAndRolesProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [ParticipationService].
 */
interface ParticipationServiceTest
{
    companion object
    {
        private const val deviceRoleName: String = "Primary"
    }


    /**
     * System under test: the [participationService] and all dependencies to be used in tests.
     */
    data class SUT(
        val participationService: ParticipationService,
        val deploymentService: DeploymentService,
        val accountService: AccountService,
        val eventBus: EventBus
    )

    /**
     * Create the system under test (SUT): the [ParticipationService] and all dependencies to be used in tests.
     */
    fun createSUT(): SUT


    @Test
    fun getActiveParticipationInvitations_succeeds() = runTest {
        val (participationService, deploymentService, accountService) = createSUT()
        val protocol = createSinglePrimaryDeviceProtocol()
        val identity = AccountIdentity.fromEmailAddress( "test@test.com" )
        val invitation = StudyInvitation( "Test study", "description", ApplicationData( "Custom data" ) )
        val participantInvitation = ParticipantInvitation(
            participantId = UUID.randomUUID(),
            AssignedTo.All,
            identity,
            invitation
        )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( participantInvitation ) )

        val account = accountService.findAccount( identity )
        assertNotNull( account )
        val retrievedInvitation = participationService.getActiveParticipationInvitations( account.id ).singleOrNull()
        assertNotNull( retrievedInvitation )
        assertEquals( studyDeploymentId, retrievedInvitation.participation.studyDeploymentId )
        assertEquals( invitation, retrievedInvitation.invitation )
        val expectedAssignedDevice = AssignedPrimaryDevice( protocol.primaryDevices.single() )
        assertEquals( setOf( expectedAssignedDevice ), retrievedInvitation.assignedDevices )
    }

    @Test
    fun getActiveParticipationInvitations_returns_invitations_specific_to_account() = runTest {
        val (participationService, deploymentService, accountService) = createSUT()
        val (protocol, device1, device2, role1, role2) = createTwoDevicesAndRolesProtocol()
        val invitation = StudyInvitation( "Test study", "description" )
        val identity1 = AccountIdentity.fromEmailAddress( "role1@test.com" )
        val identity2 = AccountIdentity.fromEmailAddress( "role2@test.com" )
        val participantId1 = UUID.randomUUID()
        val participantId2 = UUID.randomUUID()
        val participantInvitation1 =
            ParticipantInvitation( participantId1, AssignedTo.Roles( setOf( role1 ) ), identity1, invitation )
        val participantInvitation2 =
            ParticipantInvitation( participantId2, AssignedTo.Roles( setOf( role2 ) ), identity2, invitation )
        deploymentService.createStudyDeployment(
            UUID.randomUUID(),
            protocol.getSnapshot(),
            listOf( participantInvitation1, participantInvitation2 )
        )

        val account1 = accountService.findAccount( identity1 )
        assertNotNull( account1 )
        val retrievedInvitation1 = participationService.getActiveParticipationInvitations( account1.id ).singleOrNull()
        assertNotNull( retrievedInvitation1 )
        assertEquals( participantId1, retrievedInvitation1.participation.participantId )
        val assignedDevice1 = retrievedInvitation1.assignedDevices.singleOrNull()?.device?.roleName
        assertEquals( device1.roleName, assignedDevice1 )

        val account2 = accountService.findAccount( identity2 )
        assertNotNull( account2 )
        val retrievedInvitation2 = participationService.getActiveParticipationInvitations( account2.id ).singleOrNull()
        assertNotNull( retrievedInvitation2 )
        assertEquals( participantId2, retrievedInvitation2.participation.participantId )
        val assignedDevice2 = retrievedInvitation2.assignedDevices.singleOrNull()?.device?.roleName
        assertEquals( device2.roleName, assignedDevice2 )
    }

    @Test
    fun getParticipantData_initially_returns_null_for_all_expected_data() = runTest {
        val (participationService, deploymentService, _) = createSUT()

        // Create protocol with one common custom attribute and 'sex' assigned to two participant roles.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        protocol.addParticipantRole( ParticipantRole( "Role 1", false ) )
        protocol.addParticipantRole( ParticipantRole( "Role 2", false ) )
        val participantRoleNames = protocol.participantRoles.map { it.role }.toSet()
        val commonExpectedData = ExpectedParticipantData(
            ParticipantAttribute.CustomParticipantAttribute( Text( "Custom" ) )
        )
        protocol.addExpectedParticipantData( commonExpectedData )
        val roleExpectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            AssignedTo.Roles( participantRoleNames )
        )
        protocol.addExpectedParticipantData( roleExpectedData )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val participantData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( studyDeploymentId, participantData.studyDeploymentId )
        assertEquals( setOf( commonExpectedData.inputDataType ), participantData.common.keys )
        assertTrue( participantData.common.values.all { it == null } )
        assertEquals( participantRoleNames, participantData.roles.map { it.roleName }.toSet() )
        assertTrue(
            participantData.roles.all {
                it.data.keys.single() == CarpInputDataTypes.SEX && it.data.values.single() == null
            }
        )
    }

    @Test
    fun getParticipantData_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( unknownId ) }
    }

    @Test
    fun getParticipantDataList_succeeds() = runTest {
        val (participationService, deploymentService, _) = createSUT()
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val protocolSnapshot = protocol.getSnapshot()
        val invitation1 = createParticipantInvitation()
        val deploymentId1 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId1, protocolSnapshot, listOf( invitation1 ) )
        val invitation2 = createParticipantInvitation()
        val deploymentId2 = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId2, protocolSnapshot, listOf( invitation2 ) )

        val deploymentIds = setOf( deploymentId1, deploymentId2 )
        val participantDataList = participationService.getParticipantDataList( deploymentIds )
        assertEquals( 2, participantDataList.size )
    }

    @Test
    fun getParticipantDataList_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createSUT()

        val deploymentIds = setOf( unknownId )
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantDataList( deploymentIds ) }
    }

    @Test
    fun setParticipantData_assigned_to_all_roles_succeeds() = runTest {
        val (participationService, deploymentService, _) = createSUT()

        // Create protocol without roles with expected 'sex' participant data.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addExpectedParticipantData( expectedData )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        val afterSet = participationService.setParticipantData( studyDeploymentId, toSet )
        assertEquals( Sex.Male, afterSet.common[ CarpInputDataTypes.SEX ] )

        val retrievedData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( afterSet, retrievedData )
    }

    @Test
    fun setParticipantData_assigned_to_role_succeeds() = runTest {
        val (participationService, deploymentService, _) = createSUT()

        // Create protocol with expected 'sex' participant data for a single participant role.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        protocol.addParticipantRole( ParticipantRole( "Role", false ) )
        val participantRoleName = protocol.participantRoles.map { it.role }.first()
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX ),
            AssignedTo.Roles( setOf( participantRoleName ) )
        )
        protocol.addExpectedParticipantData( expectedData )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        val afterSet = participationService.setParticipantData( studyDeploymentId, toSet, participantRoleName )
        assertEquals(
            Sex.Male,
            afterSet.roles.firstOrNull { it.roleName == participantRoleName }?.data?.get( CarpInputDataTypes.SEX )
        )

        val retrievedData = participationService.getParticipantData( studyDeploymentId )
        assertEquals( afterSet, retrievedData )
    }

    @Test
    fun setParticipantData_fails_for_unknown_deploymentId() = runTest {
        val (participationService, _, _) = createSUT()

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( unknownId, toSet )
        }
    }

    @Test
    fun setParticipantData_fails_for_unexpected_input_for_protocol() = runTest {
        val (participationService, deploymentService, _) = createSUT()
        val studyDeploymentId = addTestDeployment( deploymentService )

        val toSet = mapOf( CarpInputDataTypes.SEX to Sex.Male )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( studyDeploymentId, toSet )
        }
    }

    @Test
    fun setParticipantData_fails_for_invalid_data() = runTest {
        val (participationService, deploymentService, _) = createSUT()

        // Create protocol with expected 'sex' participant data.
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( CarpInputDataTypes.SEX )
        )
        protocol.addExpectedParticipantData( expectedData )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        val toSet = mapOf( CarpInputDataTypes.SEX to StubDataPoint() )
        assertFailsWith<IllegalArgumentException>
        {
            participationService.setParticipantData( studyDeploymentId, toSet )
        }
    }

    /**
     * Add a test deployment to [deploymentService] for a protocol with a single primary device with [deviceRoleName].
     */
    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        return studyDeploymentId
    }
}
