package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.createParticipantInvitation
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlinx.coroutines.test.runTest
import kotlin.test.*


private val unknownId: UUID = UUID.randomUUID()


/**
 * Tests for implementations of [DeploymentService].
 */
interface DeploymentServiceTest
{
    /**
     * System under test: the [deploymentService] and all dependencies to be used in tests.
     */
    data class SUT( val deploymentService: DeploymentService, val eventBus: EventBus )

    /**
     * Create the system under test (SUT): the [DeploymentService] and all dependencies to be used in tests.
     */
    fun createSUT(): SUT


    @Test
    fun createStudyDeployment_registers_preregistered_devices() = runTest {
        val (service, _) = createSUT()
        val (protocol, primaryDevice, connectedDevice) = createSinglePrimaryWithConnectedDeviceProtocol()

        val deploymentId = UUID.randomUUID()
        val preregistration = connectedDevice.createRegistration()
        service.createStudyDeployment(
            deploymentId,
            protocol.getSnapshot(),
            listOf( createParticipantInvitation() ),
            mapOf( connectedDevice.roleName to preregistration )
        )
        service.registerDevice( deploymentId, primaryDevice.roleName, primaryDevice.createRegistration() )

        val deployment = service.getDeviceDeploymentFor( deploymentId, primaryDevice.roleName )
        assertEquals( preregistration, deployment.connectedDeviceRegistrations[ connectedDevice.roleName ] )
    }

    @Test
    fun createStudyDeployment_fails_for_existing_id() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Primary" )

        val deviceRole = "Test device"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRole )
        val invitation = ParticipantInvitation(
            UUID.randomUUID(),
            AssignedTo.All,
            AccountIdentity.fromUsername( "User" ),
            StudyInvitation( "Some study" )
        )
        assertFailsWith<IllegalArgumentException> {
            service.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )
        }
    }

    @Test
    fun removeStudyDeployments_succeeds() = runTest {
        val (service, _) = createSUT()
        val deploymentId1 = addTestDeployment( service, "Test device" )
        val deploymentId2 = addTestDeployment( service, "Test device" )
        val deploymentIds = setOf( deploymentId1, deploymentId2 )

        val removedIds = service.removeStudyDeployments( deploymentIds )
        assertEquals( deploymentIds, removedIds )
        assertFailsWith<IllegalArgumentException> { service.getStudyDeploymentStatus( deploymentId1 ) }
        assertFailsWith<IllegalArgumentException> { service.getStudyDeploymentStatus( deploymentId2 ) }
    }

    @Test
    fun removeStudyDeployments_ignores_unknown_ids() = runTest {
        val (service, _) = createSUT()
        val deploymentId = addTestDeployment( service, "Test device" )
        val unknownId = UUID.randomUUID()
        val deploymentIds = setOf( deploymentId, unknownId )

        val removedIds = service.removeStudyDeployments( deploymentIds )
        assertEquals( setOf( deploymentId ), removedIds )
    }

    @Test
    fun getStudyDeploymentStatus_succeeds() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Test device" )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        service.getStudyDeploymentStatus( studyDeploymentId )
    }

    @Test
    fun getStudyDeploymentStatus_fails_for_unknown_studyDeploymentId() = runTest {
        val (service, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { service.getStudyDeploymentStatus( unknownId ) }
    }

    @Test
    fun getStudyDeploymentStatusList_succeeds() = runTest {
        val (service, _) = createSUT()
        val deviceRoleName = "Primary"
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol( deviceRoleName )
        val protocolSnapshot = protocol.getSnapshot()

        val invitation1 = createParticipantInvitation( AccountIdentity.fromUsername( "User 1" ) )
        val deploymentId1 = UUID.randomUUID()
        service.createStudyDeployment( deploymentId1, protocolSnapshot, listOf( invitation1 ) )
        val invitation2 = createParticipantInvitation( AccountIdentity.fromUsername( "User 2" ) )
        val deploymentId2 = UUID.randomUUID()
        service.createStudyDeployment( deploymentId2, protocolSnapshot, listOf( invitation2 ) )

        // Actual testing of the status responses should already be covered adequately in StudyDeployment tests.
        service.getStudyDeploymentStatusList( setOf( deploymentId1, deploymentId2 ) )
    }

    @Test
    fun getStudyDeploymentStatusList_fails_when_containing_an_unknown_studyDeploymentId() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Test device" )

        val deploymentIds = setOf( studyDeploymentId, unknownId )
        assertFailsWith<IllegalArgumentException> { service.getStudyDeploymentStatusList( deploymentIds ) }
    }

    @Test
    fun registerDevice_can_be_called_multiple_times() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Primary" )
        val status = service.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }

        val registration = primary.createRegistration()
        val firstRegisterStatus = service.registerDevice( studyDeploymentId, primary.roleName, registration )
        val secondRegisterStatus = service.registerDevice( studyDeploymentId, primary.roleName, registration )
        assertEquals( firstRegisterStatus, secondRegisterStatus )
    }

    @Test
    fun registerDevice_cannot_be_called_with_same_registration_when_stopped() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Primary" )
        val status = service.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }
        val registration = primary.createRegistration()
        service.registerDevice( studyDeploymentId, primary.roleName, registration )
        service.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
        {
            service.registerDevice( studyDeploymentId, primary.roleName, registration )
        }
    }

    @Test
    fun unregisterDevice_succeeds() = runTest {
        val (service, _) = createSUT()
        val deviceRolename = "Test device"
        val studyDeploymentId = addTestDeployment( service, deviceRolename )
        var status = service.getStudyDeploymentStatus( studyDeploymentId )
        val device = status.getRemainingDevicesToRegister().first { it.roleName == deviceRolename }
        service.registerDevice( studyDeploymentId, deviceRolename, device.createRegistration { } )

        status = service.unregisterDevice( studyDeploymentId, deviceRolename )
        assertTrue( device in status.getRemainingDevicesToRegister() )
    }

    @Test
    fun getDeviceDeploymentFor_during_device_reregistrations() = runTest {
        val (service, _) = createSUT()
        val protocol = createSinglePrimaryDeviceProtocol( "Test device" )
        val device = protocol.primaryDevices.first()
        val deploymentId = UUID.randomUUID()
        service.createStudyDeployment( deploymentId, protocol.getSnapshot(), listOf( createParticipantInvitation() ) )
        val firstRegistration = device.createRegistration()
        service.registerDevice( deploymentId, device.roleName, firstRegistration )

        val firstDeployment: PrimaryDeviceDeployment = service.getDeviceDeploymentFor( deploymentId, device.roleName )
        assertEquals( firstRegistration, firstDeployment.registration )

        service.unregisterDevice( deploymentId, device.roleName )
        assertFailsWith<IllegalArgumentException> { service.getDeviceDeploymentFor( deploymentId, device.roleName ) }

        val secondRegistration = device.createRegistration()
        service.registerDevice( deploymentId, device.roleName, secondRegistration )
        val secondDeployment = service.getDeviceDeploymentFor( deploymentId, device.roleName )
        assertEquals( secondRegistration, secondDeployment.registration )
    }

    @Test
    fun deviceDeployed_succeeds() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Primary", "Connected" )
        var status = service.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }
        val connected = status.getRemainingDevicesToRegister().first { it.roleName == "Connected" }
        service.registerDevice( studyDeploymentId, primary.roleName, primary.createRegistration() )
        service.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() )

        val deployment = service.getDeviceDeploymentFor( studyDeploymentId, primary.roleName )
        status = service.deviceDeployed( studyDeploymentId, primary.roleName, deployment.lastUpdatedOn )

        val runningStatus = status as? StudyDeploymentStatus.Running
        assertNotNull( runningStatus )
        val primaryDeviceStatus = runningStatus.deviceStatusList.single { it.device == primary }
        assertTrue( primaryDeviceStatus is DeviceDeploymentStatus.Deployed )
        val connectedDeviceStatus = runningStatus.deviceStatusList.single { it.device == connected }
        assertTrue( connectedDeviceStatus is DeviceDeploymentStatus.Registered )
    }

    @Test
    fun stop_succeeds() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Test device" )

        val status = service.stop( studyDeploymentId )
        assertTrue( status is StudyDeploymentStatus.Stopped )
    }

    @Test
    fun stop_fails_for_unknown_studyDeploymentId() = runTest {
        val (service, _) = createSUT()

        assertFailsWith<IllegalArgumentException> { service.stop( unknownId ) }
    }

    @Test
    fun modifications_after_stop_not_allowed() = runTest {
        val (service, _) = createSUT()
        val studyDeploymentId = addTestDeployment( service, "Primary", "Connected" )
        val status = service.getStudyDeploymentStatus( studyDeploymentId )
        val primary = status.getRemainingDevicesToRegister().first { it.roleName == "Primary" }
        val connected = status.getRemainingDevicesToRegister().first { it.roleName == "Connected" }
        service.registerDevice( studyDeploymentId, primary.roleName, primary.createRegistration() )
        service.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() )
        service.stop( studyDeploymentId )

        assertFailsWith<IllegalStateException>
            { service.registerDevice( studyDeploymentId, connected.roleName, connected.createRegistration() ) }
        assertFailsWith<IllegalStateException>
            { service.unregisterDevice( studyDeploymentId, primary.roleName ) }
        val deviceDeployment = service.getDeviceDeploymentFor( studyDeploymentId, primary.roleName )
        assertFailsWith<IllegalStateException>
            { service.deviceDeployed( studyDeploymentId, primary.roleName, deviceDeployment.lastUpdatedOn ) }
    }


    /**
     * Create a deployment to be used in tests in the given [service] with a protocol
     * containing a single primary device with the specified [primaryDeviceRoleName]
     * and a connected device, of which the [connectedDeviceRoleName] can optionally be defined.
     */
    private suspend fun addTestDeployment(
        service: DeploymentService,
        primaryDeviceRoleName: String,
        connectedDeviceRoleName: String = "Connected"
    ): UUID
    {
        val (protocol, _, _) =
            createSinglePrimaryWithConnectedDeviceProtocol( primaryDeviceRoleName, connectedDeviceRoleName )
        val invitation = createParticipantInvitation()
        val studyDeploymentId = UUID.randomUUID()
        service.createStudyDeployment( studyDeploymentId, protocol.getSnapshot(), listOf( invitation ) )

        return studyDeploymentId
    }
}
