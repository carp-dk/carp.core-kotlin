package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.SensorData
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.infrastructure.test.STUB_DATA_POINT_TYPE
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.common.infrastructure.test.StubPrimaryDeviceConfiguration
import dk.cachet.carp.common.infrastructure.test.StubTaskConfiguration
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.getDataType
import dk.cachet.carp.data.infrastructure.measurement
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createComplexProtocol
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests whether different application service hosts relied upon by [DeploymentService]
 * are correctly integrated through integration events.
 */
class HostsIntegrationTest
{
    private lateinit var eventBus: EventBus

    private lateinit var accountService: AccountService
    private lateinit var deploymentService: DeploymentService
    private lateinit var participationService: ParticipationService
    private lateinit var dataStreamService: DataStreamService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        dataStreamService = InMemoryDataStreamService()

        // Create deployment service.
        deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            dataStreamService,
            eventBus.createApplicationServiceAdapter( DeploymentService::class )
        )

        // Create participation service.
        accountService = InMemoryAccountService()
        participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( accountService ),
            eventBus.createApplicationServiceAdapter( ParticipationService::class )
        )
    }

    @Test
    fun create_deployment_creates_participant_group() = runTest {
        var deploymentCreated: DeploymentService.Event.StudyDeploymentCreated? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentCreated::class, this )
        {
            deploymentCreated = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol()
        val invitation = createParticipantInvitation()
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )
        val participantGroupData = participationService.getParticipantData( deploymentId )

        assertEquals( deploymentId, deploymentCreated?.studyDeploymentId )
        val commonExpectedData = protocol.expectedParticipantData.filter { it.assignedTo == AssignedTo.All }
        assertEquals( commonExpectedData.size, participantGroupData.common.size )
    }

    @Test
    fun create_deployment_adds_participations() = runTest {
        val deviceRole = "Phone"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRole )

        // Create deployment with one invitation.
        val toInvite = EmailAccountIdentity( "test@test.com" )
        val studyInvitation = StudyInvitation( "Some study" )
        val invitations = listOf(
            ParticipantInvitation( UUID.randomUUID(), AssignedTo.All, toInvite, studyInvitation )
        )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), invitations )

        // Verify whether the invitation was sent.
        val invitedAccount = accountService.findAccount( toInvite )
        assertNotNull( invitedAccount )
        val invitation = participationService.getActiveParticipationInvitations( invitedAccount.id ).singleOrNull()
        assertNotNull( invitation )
        assertEquals( deploymentId, invitation.participation.studyDeploymentId )
        assertEquals( setOf( deviceRole ), invitation.assignedDevices.map { it.device.roleName }.toSet() )
        assertEquals( studyInvitation, invitation.invitation )
    }

    @Test
    fun deviceDeployed_opens_data_streams() = runTest {
        // Create a device deployment for a protocol with a known data stream.
        val protocol = createSinglePrimaryDeviceProtocol()
        addPrimaryDeviceDataStream<StubDataPoint>( protocol )
        val deploymentId = createSinglePrimaryDeviceDeployment( protocol )

        val primaryDevice = protocol.primaryDevices.single()
        val dataStreamId = dataStreamId<StubDataPoint>( deploymentId, primaryDevice.roleName )
        val stream = dataStreamService.getDataStream( dataStreamId, 0 )
        assertTrue( stream.isEmpty() )
    }

    @Test
    fun deviceDeployed_second_time_after_reregistration_succeeds() = runTest {
        // Create a device deployment and unregister the device after.
        val protocol = createSinglePrimaryDeviceProtocol()
        val deploymentId = createSinglePrimaryDeviceDeployment( protocol )
        val primaryDevice = protocol.primaryDevices.single()
        deploymentService.unregisterDevice( deploymentId, primaryDevice.roleName )

        // Redeploy device.
        deploymentService.registerDevice( deploymentId, primaryDevice.roleName, primaryDevice.createRegistration() )
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( deploymentId, primaryDevice.roleName )
        val deploymentStatus =
            deploymentService.deviceDeployed( deploymentId, primaryDevice.roleName, deviceDeployment.lastUpdatedOn )
        assertTrue( deploymentStatus is StudyDeploymentStatus.Running )
    }

    @Test
    fun removing_deployment_removes_participant_group_and_data_streams() = runTest {
        var deploymentRemoved: DeploymentService.Event.StudyDeploymentRemoved? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentRemoved::class, this )
        {
            deploymentRemoved = it
        }
        eventBus.activateHandlers( this )

        // Create a device deployment for a protocol with a known data stream.
        val protocol = createSinglePrimaryDeviceProtocol()
        addPrimaryDeviceDataStream<StubDataPoint>( protocol )
        val deploymentId = createSinglePrimaryDeviceDeployment( protocol )

        deploymentService.removeStudyDeployments( setOf( deploymentId ) )

        assertEquals( deploymentId, deploymentRemoved?.studyDeploymentId )
        val primaryDevice = protocol.primaryDevices.single()
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( deploymentId ) }
        val dataStreamId = dataStreamId<StubDataPoint>( deploymentId, primaryDevice.roleName )
        assertFailsWith<IllegalArgumentException> { dataStreamService.getDataStream( dataStreamId, 0 ) }
    }

    /**
     * Creates a deployment using [deploymentService] for a [protocol] that only contains a single primary device,
     * and registers and deploys the primary devices.
     */
    private suspend fun createSinglePrimaryDeviceDeployment( protocol: StudyProtocol ): UUID
    {
        val deploymentId = UUID.randomUUID()
        val invitation = createParticipantInvitation()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )
        val primaryDevice = protocol.primaryDevices.single()
        deploymentService.registerDevice( deploymentId, primaryDevice.roleName, primaryDevice.createRegistration() )
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( deploymentId, primaryDevice.roleName )
        deploymentService.deviceDeployed( deploymentId, primaryDevice.roleName, deviceDeployment.lastUpdatedOn )

        return deploymentId
    }

    /**
     * Update the protocol to measure [TData] at the start of the study on the primary device.
     */
    private inline fun <reified TData : SensorData> addPrimaryDeviceDataStream( protocol: StudyProtocol )
    {
        val dataType = getDataType( TData::class )
        val primaryDevice = protocol.primaryDevices.single()
        val task = StubTaskConfiguration( "Task", listOf( Measure.DataStream( dataType ) ) )
        val atStartOfStudy = protocol.addTrigger( primaryDevice.atStartOfStudy() )
        protocol.addTaskControl( atStartOfStudy.start( task, primaryDevice ) )
    }

    @Test
    fun stopping_deployment_stops_participant_group() = runTest {
        var studyDeploymentStopped: DeploymentService.Event.StudyDeploymentStopped? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentStopped::class, this )
        {
            studyDeploymentStopped = it
        }
        eventBus.activateHandlers( this )

        val protocol = createComplexProtocol()
        val invitation = createParticipantInvitation()
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )
        deploymentService.stop( deploymentId )

        assertEquals( deploymentId, studyDeploymentStopped?.studyDeploymentId )
    }

    @Test
    fun registration_changes_in_deployment_are_passed_to_participant_group() = runTest {
        // Create a deployment.
        val protocol = createSinglePrimaryDeviceProtocol()
        val identity = AccountIdentity.fromUsername( "Test" )
        val invitation = createParticipantInvitation( identity )
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( invitation ) )

        // Subscribe to registration changes to test whether integration events are sent.
        var registrationChanged: DeploymentService.Event.DeviceRegistrationChanged? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.DeviceRegistrationChanged::class, this )
        {
            registrationChanged = it
        }
        eventBus.activateHandlers( this )

        // Change registration for the assigned device.
        val assignedDevice = protocol.primaryDevices.single()
        val account = accountService.findAccount( identity )!!
        val registration = assignedDevice.createRegistration()
        deploymentService.registerDevice( deploymentId, assignedDevice.roleName, registration )
        assertEquals( assignedDevice, registrationChanged?.device )
        assertEquals( registration, registrationChanged?.registration )
        var invitations = participationService.getActiveParticipationInvitations( account.id ).single()
        assertEquals( registration, invitations.assignedDevices.single().registration )

        // Remove registration for the assigned device.
        deploymentService.unregisterDevice( deploymentId, assignedDevice.roleName )
        assertEquals( assignedDevice, registrationChanged?.device )
        assertNull( registrationChanged?.registration )
        invitations = participationService.getActiveParticipationInvitations( account.id ).single()
        assertNull( invitations.assignedDevices.single().registration )
    }

    @Test
    fun appendToDataStreams_succeeds_as_long_as_deployment_is_ready() = runTest {
        // Create a protocol which when deployed has one `STUB_DATA_TYPE` data stream.
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val protocol = createEmptyProtocol()
        protocol.addPrimaryDevice( primaryDevice )
        val task = StubTaskConfiguration( "Task", listOf( Measure.DataStream( STUB_DATA_POINT_TYPE ) ) )
        val atStartOfStudy = protocol.addTrigger( primaryDevice.atStartOfStudy() )
        protocol.addTaskControl( atStartOfStudy.start( task, primaryDevice ) )

        // Deploy protocol.
        val deploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment(
            deploymentId,
            protocol.getSnapshot(),
            listOf( createParticipantInvitation( AccountIdentity.fromUsername( "Test" ) ) )
        )

        // Prepare data to append to data stream.
        val stubStreamId = dataStreamId<StubDataPoint>( deploymentId, primaryDevice.roleName )
        val now = Clock.System.now()
        val syncPoint = SyncPoint( now, now.toEpochMicroseconds() )
        val toAppend = MutableDataStreamBatch()
        toAppend.appendSequence(
            MutableDataStreamSequence<StubDataPoint>( stubStreamId, 0, listOf( atStartOfStudy.id ), syncPoint )
                .apply { appendMeasurements( measurement( StubDataPoint(), 0 ) ) }
        )

        // Data streams aren't open yet since deployment is not ready.
        assertFailsWith<IllegalArgumentException> { dataStreamService.appendToDataStreams( deploymentId, toAppend ) }

        // Data can now be appended to data streams once the deployment is "ready".
        deploymentService.registerDevice( deploymentId, primaryDevice.roleName, primaryDevice.createRegistration() )
        val deviceDeployment = deploymentService.getDeviceDeploymentFor( deploymentId, primaryDevice.roleName )
        deploymentService.deviceDeployed( deploymentId, primaryDevice.roleName, deviceDeployment.lastUpdatedOn )
        dataStreamService.appendToDataStreams( deploymentId, toAppend )

        // Data can no longer be appended after a deployment is stopped.
        deploymentService.stop( deploymentId )
        val appendMore = MutableDataStreamBatch()
        appendMore.appendSequence(
            MutableDataStreamSequence<StubDataPoint>( stubStreamId, 1, listOf( atStartOfStudy.id ), syncPoint )
                .apply { appendMeasurements( measurement( StubDataPoint(), 1000 ) ) }
        )
        assertFailsWith<IllegalStateException> { dataStreamService.appendToDataStreams( deploymentId, appendMore ) }
    }
}
