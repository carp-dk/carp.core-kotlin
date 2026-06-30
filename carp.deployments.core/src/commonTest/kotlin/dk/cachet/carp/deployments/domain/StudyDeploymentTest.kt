@file:Suppress( "LargeClass" )

package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.ApplicationData
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.devices.*
import dk.cachet.carp.common.application.tasks.Measure
import dk.cachet.carp.common.application.triggers.TaskControl
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.common.infrastructure.test.*
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.ParticipantStatus
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.protocols.infrastructure.test.createEmptyProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryWithConnectedDeviceProtocol
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests for [StudyDeployment].
 */
@Suppress( "LargeClass" )
class StudyDeploymentTest
{
    companion object
    {
        private val JSON: Json = createTestJSON()
    }


    @Test
    fun fromInvitations_with_invalid_protocol_fails()
    {
        val protocol = createEmptyProtocol()
        val snapshot = protocol.getSnapshot()

        // Protocol does not contain a primary device, thus contains deployment error and can't be initialized.
        assertFailsWith<IllegalArgumentException>
        {
            StudyDeployment.fromInvitations( snapshot, emptyList() )
        }
    }

    @Test
    fun fromInvitations_with_invalid_invitations_fails()
    {
        val protocol = createSinglePrimaryDeviceProtocol()
        val snapshot = protocol.getSnapshot()

        val incorrectInvitation = ParticipantInvitation(
            UUID.randomUUID(),
            AssignedTo.Roles( setOf( "Invalid" ) ),
            UsernameAccountIdentity( "Test" ),
            StudyInvitation( "Test" )
        )
        assertFailsWith<IllegalArgumentException>
        {
            StudyDeployment.fromInvitations( snapshot, listOf( incorrectInvitation ) )
        }
    }

    @Test
    fun new_deployment_has_unregistered_primary_device()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Two devices can be registered, but none are by default.
        assertEquals( 2, deployment.registrableDevices.size )
        assertTrue { deployment.registrableDevices.map { it.device }.containsAll( protocol.devices ) }
        assertEquals( 0, deployment.registeredDevices.size )

        // Only the primary device requires deployment.
        val requiredDeployment = deployment.registrableDevices.single { it.requiresDeployment }
        assertEquals( primary, requiredDeployment.device )
    }

    @Test
    fun requiredDataStreams_is_complete()
    {
        val primaryDevice = StubPrimaryDeviceConfiguration()
        val connectedDevice = StubDeviceConfiguration()
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( primaryDevice )
            addConnectedDevice( connectedDevice, primaryDevice )

            val trigger = addTrigger( primaryDevice.atStartOfStudy() )
            val stubMeasure = Measure.DataStream( STUB_DATA_POINT_TYPE )
            val task = StubTaskConfiguration(
                "Task",
                listOf( stubMeasure, trigger.measure() ),
                "Description"

            )
            addTaskControl( trigger.start( task, primaryDevice ) )
            val connectedDeviceTask = StubTaskConfiguration( "Connected task", listOf( stubMeasure ) )
            addTaskControl( trigger.start( connectedDeviceTask, connectedDevice ) )
        }
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val dataStreams = deployment.requiredDataStreams
        assertEquals( deployment.id, dataStreams.studyDeploymentId )
        val expectedPrimaryDeviceTypes =
            listOf( STUB_DATA_POINT_TYPE, CarpDataTypes.TRIGGERED_TASK.type, CarpDataTypes.COMPLETED_TASK.type )
                .map { DataStreamsConfiguration.ExpectedDataStream( primaryDevice.roleName, it ) }
        val expectedConnectedDeviceType =
            listOf( STUB_DATA_POINT_TYPE, CarpDataTypes.COMPLETED_TASK.type )
                .map { DataStreamsConfiguration.ExpectedDataStream( connectedDevice.roleName, it ) }
        assertEquals(
            ( expectedPrimaryDeviceTypes + expectedConnectedDeviceType ).toSet(),
            dataStreams.expectedDataStreams
        )
    }

    @Test
    fun registerDevice_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( device, registration )

        assertEquals( 1, deployment.registeredDevices.size )
        val registered = deployment.registeredDevices.values.single()
        assertEquals( registration, registered )
        assertEquals( 1, deployment.deviceRegistrationHistory[ device ]?.count() )
        assertEquals( registration, deployment.deviceRegistrationHistory[ device ]?.last() )
        assertEquals( StudyDeployment.Event.DeviceRegistered( device, registration ), deployment.consumeEvents().last() )
    }

    @Test
    fun registerDevice_of_optional_primary_device_triggers_redeployment()
    {
        // Deploy primary device.
        val primary = StubPrimaryDeviceConfiguration( "Primary 1" )
        val optionalPrimary = StubPrimaryDeviceConfiguration( "Primary 2", true )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( primary )
            addPrimaryDevice( optionalPrimary )
        }
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, primary.createRegistration() )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Running )

        // Register dependent device.
        deployment.registerDevice( optionalPrimary, optionalPrimary.createRegistration() )
        val status = deployment.getStatus()
        assertTrue( status is StudyDeploymentStatus.DeployingDevices )
        val deviceStatus = status.getDeviceStatus( primary )
        assertTrue( deviceStatus is DeviceDeploymentStatus.NeedsRedeployment )
    }

    @Test
    fun cant_registerDevice_not_part_of_deployment()
    {
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val invalidDevice = StubPrimaryDeviceConfiguration( "Not part of deployment" )
        val registration = DefaultDeviceRegistration()

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( invalidDevice, registration )
        }
        assertEquals( 0, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceRegistered>().count() )
    }

    @Test
    fun cant_registerDevice_if_already_registered()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        deployment.registerDevice( device, DefaultDeviceRegistration() )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( device, DefaultDeviceRegistration() )
        }
        assertEquals( 1, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceRegistered>().count() )
    }

    /**
     * When the runtime type of devices is unknown, deployment cannot verify whether a registration is valid (this is implemented on the type definition).
     * However, rather than not supporting deployment, registration is simply considered valid and forwarded as is.
     */
    @Test
    fun can_registerDevice_for_unknown_types()
    {
        val protocol = createEmptyProtocol()
        val primaryUnknown = StubPrimaryDeviceConfiguration( "Unknown primary" ).makeUnknown( JSON )
        val connectedUnknown = StubDeviceConfiguration( "Unknown connected" ).makeUnknown( JSON )

        protocol.addPrimaryDevice( primaryUnknown )
        protocol.addConnectedDevice( connectedUnknown, primaryUnknown )

        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primaryUnknown, DefaultDeviceRegistration() )
        deployment.registerDevice( connectedUnknown, DefaultDeviceRegistration() )
    }

    @Test
    fun cant_registerDevice_already_in_use_by_different_role()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        val connected = StubPrimaryDeviceConfiguration( "Connected" )
        protocol.addPrimaryDevice( primary )
        protocol.addConnectedDevice( connected, primary )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( primary, registration )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( connected, registration )
        }
    }

    @Test
    fun can_registerDevice_with_same_id_for_two_different_unknown_types()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration()
        val device1Unknown = StubPrimaryDeviceConfiguration( "Unknown device 1" )
            .makeUnknown( JSON, "One class" )
        val device2Unknown = StubDeviceConfiguration( "Unknown device 2" )
            .makeUnknown( JSON, "Not the same class" )

        protocol.addPrimaryDevice( primary )
        protocol.addConnectedDevice( device1Unknown, primary )
        protocol.addConnectedDevice( device2Unknown, primary )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Even though these two devices are registered using the same ID, this should succeed since they are of different types.
        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( device1Unknown, registration )
        deployment.registerDevice( device2Unknown, registration )
    }

    @Test
    fun cant_registerDevice_with_wrong_registration_type()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        protocol.addPrimaryDevice( primary )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val wrongRegistration = AltBeaconDeviceRegistration( 0, UUID.randomUUID(), 0, 0, 0 )
        assertFailsWith<IllegalArgumentException>
        {
            deployment.registerDevice( primary, wrongRegistration )
        }
        assertEquals( 0, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceRegistered>().count() )
    }

    @Test
    fun unregisterDevice_with_single_device_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( device, registration )

        deployment.unregisterDevice( device )
        assertEquals( 0, deployment.registeredDevices.size )
        assertEquals( 1, deployment.deviceRegistrationHistory[ device ]?.count() )
        assertEquals( registration, deployment.deviceRegistrationHistory[ device ]?.last() )
        assertEquals( StudyDeployment.Event.DeviceUnregistered( device ), deployment.consumeEvents().last() )
        assertTrue( deployment.getStatus().getDeviceStatus( device ) is DeviceDeploymentStatus.Unregistered )
    }

    @Test
    fun unregisterDevice_for_connected_device_succeeds()
    {
        // Create deployment with registered devices.
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, primary.createRegistration() )
        deployment.registerDevice( connected, connected.createRegistration() )

        // Unregister device before device deployment obtained.
        deployment.unregisterDevice( connected )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.DeployingDevices )

        // Obtain device deployment.
        deployment.registerDevice( connected, connected.createRegistration() )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )

        // Unregister device after device deployment obtained.
        deployment.unregisterDevice( connected )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Running )
    }

    @Test
    fun unregisterDevice_invalidates_dependent_deployments()
    {
        val protocol = createEmptyProtocol()
        val primary1 = StubPrimaryDeviceConfiguration( "Primary 1" )
        protocol.addPrimaryDevice( primary1 )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2" )
        protocol.addPrimaryDevice( primary2 )
        // TODO: For now, there is no dependency between these two devices, it is simply assumed in the current implementation.
        //       This test will fail once this implementation is improved.
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary1, primary1.createRegistration { } )
        deployment.registerDevice( primary2, primary2.createRegistration { } )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary1 )
        deployment.deviceDeployed( primary1, deviceDeployment.lastUpdatedOn )

        deployment.unregisterDevice( primary2 )
        assertEquals( 0, deployment.deployedDevices.count() )
        assertEquals( setOf( primary1 ), deployment.invalidatedDeployedDevices )
        val studyStatus = deployment.getStatus()
        assertTrue( studyStatus is StudyDeploymentStatus.DeployingDevices )
        val primary1Status = studyStatus.getDeviceStatus( primary1 )
        assertTrue( primary1Status is DeviceDeploymentStatus.NeedsRedeployment )
        assertEquals( StudyDeployment.Event.DeploymentInvalidated( primary1 ), deployment.consumeEvents().last() )
    }

    @Test
    fun unregisterDevice_fails_for_device_not_part_of_deployment()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException> { deployment.unregisterDevice( primary ) }
    }

    @Test
    fun unregisterDevice_fails_for_device_which_is_not_registered()
    {
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val invalidDevice = StubPrimaryDeviceConfiguration( "Not part of deployment" )
        assertFailsWith<IllegalArgumentException> { deployment.unregisterDevice( invalidDevice ) }
    }

    @Test
    fun creating_deployment_fromSnapshot_obtained_by_getSnapshot_is_the_same()
    {
        val deployment = createComplexDeployment()

        val snapshot: StudyDeploymentSnapshot = deployment.getSnapshot()
        val fromSnapshot = StudyDeployment.fromSnapshot( snapshot )

        assertEquals( deployment.id, fromSnapshot.id )
        assertEquals( deployment.createdOn, fromSnapshot.createdOn )
        assertEquals( deployment.protocolSnapshot, fromSnapshot.protocolSnapshot )
        assertEquals(
            deployment.registrableDevices.count(),
            deployment.registrableDevices.intersect( fromSnapshot.registrableDevices ).count()
        )
        assertEquals(
            deployment.registeredDevices.count(),
            deployment.registeredDevices.entries.intersect( fromSnapshot.registeredDevices.entries ).count()
        )
        assertEquals(
            deployment.deviceRegistrationHistory.count(),
            deployment.deviceRegistrationHistory.entries.intersect( fromSnapshot.deviceRegistrationHistory.entries ).count()
        )
        assertEquals(
            deployment.deployedDevices.count(),
            deployment.deployedDevices.intersect( fromSnapshot.deployedDevices ).count()
        )
        assertEquals(
            deployment.invalidatedDeployedDevices.count(),
            deployment.invalidatedDeployedDevices.intersect( fromSnapshot.invalidatedDeployedDevices ).count()
        )
        assertEquals( deployment.startedOn, fromSnapshot.startedOn )
        assertEquals( deployment.isStopped, fromSnapshot.isStopped )
        assertEquals( 0, fromSnapshot.consumeEvents().size )
    }

    @Test
    fun fromSnapshot_succeeds_with_rich_registration_history()
    {
        val deployment: StudyDeployment = createActiveDeployment( "Primary" )
        val primary: AnyPrimaryDeviceConfiguration =
            deployment.protocol.devices.first { it.roleName == "Primary" } as AnyPrimaryDeviceConfiguration

        // Create registration history with two registrations for primary.
        val registration1 = primary.createRegistration()
        val registration2 = primary.createRegistration()
        deployment.registerDevice( primary, registration1 )
        deployment.unregisterDevice( primary )
        deployment.registerDevice( primary, registration2 )

        val snapshot = deployment.getSnapshot()
        val fromSnapshot = StudyDeployment.fromSnapshot( snapshot )
        assertEquals( listOf( registration1, registration2 ), fromSnapshot.deviceRegistrationHistory[ primary ] )
    }

    @Test
    fun fromSnapshot_succeeds_for_deployed_device_with_unregistered_connected_devices()
    {
        // Create deployment.
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Deploy device.
        deployment.registerDevice( primary, primary.createRegistration() )
        deployment.registerDevice( connected, connected.createRegistration() )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )

        // Unregister connected device after deployment.
        deployment.unregisterDevice( connected )

        val snapshot = deployment.getSnapshot()
        val loaded = StudyDeployment.fromSnapshot( snapshot )
        assertTrue( connected !in loaded.registeredDevices )
    }

    @Test
    fun getStatus_contains_invited_participants()
    {
        val deviceRoleName = "Primary"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRoleName )
        val invitation = ParticipantInvitation(
            UUID.randomUUID(),
            AssignedTo.All,
            UsernameAccountIdentity( "Test" ),
            StudyInvitation( "Test " )
        )
        val deployment = StudyDeployment.fromInvitations( protocol.getSnapshot(), listOf( invitation ) )

        val expectedParticipantStatus = ParticipantStatus(
            invitation.participantId,
            AssignedTo.All,
            setOf( deviceRoleName )
        )
        val status = deployment.getStatus()
        assertEquals( listOf( expectedParticipantStatus ), status.participantStatusList )
    }

    @Test
    fun getStatus_lifecycle_primary_and_connected()
    {
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        // Start of deployment, no devices registered.
        val status: StudyDeploymentStatus = deployment.getStatus()
        assertEquals( deployment.id, status.studyDeploymentId )
        assertEquals( 2, status.deviceStatusList.count() )
        assertTrue { status.deviceStatusList.any { it.device == primary } }
        assertTrue { status.deviceStatusList.any { it.device == connected } }
        assertTrue( status is StudyDeploymentStatus.Invited )
        val toRegister = status.getRemainingDevicesToRegister()
        val expectedToRegister = setOf<AnyDeviceConfiguration>( primary, connected )
        assertEquals( expectedToRegister, toRegister )
        assertTrue( status.getRemainingDevicesReadyToDeploy().isEmpty() )

        // After registering primary device, primary device is ready for deployment.
        deployment.registerDevice( primary, primary.createRegistration() )
        val afterPrimaryRegistered = deployment.getStatus()
        assertTrue( afterPrimaryRegistered is StudyDeploymentStatus.DeployingDevices )
        assertEquals( 1, afterPrimaryRegistered.getRemainingDevicesToRegister().count() )
        assertEquals( setOf( primary), afterPrimaryRegistered.getRemainingDevicesReadyToDeploy() )

        // After registering connected device, no more devices need to be registered.
        deployment.registerDevice( connected, connected.createRegistration() )
        val afterAllRegisterSed = deployment.getStatus()
        assertTrue( afterAllRegisterSed is StudyDeploymentStatus.DeployingDevices )
        assertEquals( 0, afterAllRegisterSed.getRemainingDevicesToRegister().count() )
        assertEquals( setOf( primary ), afterAllRegisterSed.getRemainingDevicesReadyToDeploy() )

        // Notify of successful primary device deployment.
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )
        val afterDeployStatus = deployment.getStatus()
        assertTrue( afterDeployStatus is StudyDeploymentStatus.Running )
        val deviceStatus = afterDeployStatus.getDeviceStatus( primary )
        assertTrue( deviceStatus is DeviceDeploymentStatus.Deployed )
        assertEquals( 0, afterDeployStatus.getRemainingDevicesReadyToDeploy().count() )
    }

    @Test
    fun getStatus_lifecycle_two_dependent_primaries()
    {
        val protocol = createEmptyProtocol()
        val primary1 = StubPrimaryDeviceConfiguration( "Primary 1" )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2" )
        protocol.addPrimaryDevice( primary1 )
        protocol.addPrimaryDevice( primary2 )
        // TODO: For now, there is no dependency between these two devices, it is simply assumed in the current implementation.
        //       This test will fail once this implementation is improved.
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary1, primary1.createRegistration() )
        deployment.registerDevice( primary2, primary2.createRegistration() )

        // Deploy first primary device.
        val primary1Deployment = deployment.getDeviceDeploymentFor( primary1 )
        deployment.deviceDeployed( primary1, primary1Deployment.lastUpdatedOn )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.DeployingDevices )

        // After deployment of the second primary device, deployment is running.
        val primary2Deployment = deployment.getDeviceDeploymentFor( primary2 )
        deployment.deviceDeployed( primary2, primary2Deployment.lastUpdatedOn )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Running )

        // Unregistering one device returns deployment to 'deploying'.
        deployment.unregisterDevice( primary1 )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.DeployingDevices )
    }

    @Test
    fun getStatus_for_protocol_with_only_optional_devices()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary", isOptional = true )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary 2", isOptional = true )
        protocol.addPrimaryDevice( primary )
        protocol.addPrimaryDevice( primary2 )
        val deployment = studyDeploymentFor( protocol )

        // At least one device needs to be deployed before deployment can be considered "Running".
        var status = deployment.getStatus()
        assertTrue( status is StudyDeploymentStatus.Invited )

        // Register device.
        deployment.registerDevice( primary, primary.createRegistration() )
        status = deployment.getStatus()
        assertTrue( status is StudyDeploymentStatus.DeployingDevices )

        // Deploy device. All other devices are optional, so deployment is "Running".
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )
        status = deployment.getStatus()
        assertTrue( status is StudyDeploymentStatus.Running )
    }

    @Test
    fun chained_primary_devices_cant_be_deployed()
    {
        val protocol = createEmptyProtocol()
        val primary = StubPrimaryDeviceConfiguration( "Primary" )
        val chained = StubPrimaryDeviceConfiguration( "Chained primary" )
        protocol.addPrimaryDevice( primary )
        protocol.addConnectedDevice( chained, primary )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        val status: StudyDeploymentStatus = deployment.getStatus()
        val chainedStatus = status.getDeviceStatus( chained )
        assertFalse { chainedStatus.canBeDeployed }
    }

    @Test
    fun getDeviceDeploymentFor_succeeds()
    {
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        protocol.applicationData = ApplicationData( "some data" )
        val primaryTask = StubTaskConfiguration( "Primary task" )
        val connectedTask = StubTaskConfiguration( "Connected task" )
        protocol.addTaskControl( primary.atStartOfStudy().start( primaryTask, primary ) )
        protocol.addTaskControl( primary.atStartOfStudy().start( connectedTask, connected ) )
        val expectedData = ExpectedParticipantData(
            ParticipantAttribute.DefaultParticipantAttribute( InputDataType( "namespace", "type" ) )
        )
        protocol.addExpectedParticipantData( expectedData )
        val deployment = studyDeploymentFor( protocol )
        val registration = DefaultDeviceRegistration()
        deployment.registerDevice( primary, registration )
        deployment.registerDevice( connected, connected.createRegistration() )

        // Later changes made to the protocol don't impact the previously created deployment.
        val ignoredPrimary = StubPrimaryDeviceConfiguration( "Ignored" )
        protocol.addPrimaryDevice( ignoredPrimary ) // Normally, this dependent device would block obtaining deployment.

        val deviceDeployment: PrimaryDeviceDeployment = deployment.getDeviceDeploymentFor( primary )
        assertEquals( registration, deviceDeployment.registration )
        assertEquals( protocol.getConnectedDevices( primary ).toSet(), deviceDeployment.connectedDevices )
        assertEquals( 1, deviceDeployment.connectedDeviceRegistrations.count() )
        assertEquals( setOf( expectedData ), deviceDeployment.expectedParticipantData )
        assertEquals( protocol.applicationData, deviceDeployment.applicationData )

        // Device deployment lists both tasks, even if one is destined for the connected device.
        assertEquals( protocol.tasks.count(), deviceDeployment.tasks.intersect( protocol.tasks ).count() )

        // Device deployment contains correct trigger information.
        assertEquals(1, deviceDeployment.triggers.count() )
        assertEquals( primary.atStartOfStudy(), deviceDeployment.triggers[ 0 ] )
        val taskControls = deviceDeployment.taskControls
        assertEquals(2, taskControls.count() )
        assertTrue( TaskControl( 0, primaryTask.name, primary.roleName, TaskControl.Control.Start ) in taskControls )
        assertTrue( TaskControl( 0, connectedTask.name, connected.roleName, TaskControl.Control.Start ) in taskControls )
    }

    @Test
    fun getDeviceDeploymentFor_with_preregistered_device_succeeds()
    {
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, DefaultDeviceRegistration() )

        val preregistration = DefaultDeviceRegistration()
        deployment.registerDevice( connected, preregistration )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )

        assertEquals( "Connected", deviceDeployment.connectedDeviceRegistrations.keys.single() )
        assertEquals( preregistration, deviceDeployment.connectedDeviceRegistrations.getValue( "Connected" ) )
    }

    @Test
    fun getDeviceDeploymentFor_without_preregistered_device_succeeds()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, DefaultDeviceRegistration() )

        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )

        assertTrue( deviceDeployment.connectedDeviceRegistrations.isEmpty() )
    }

    @Test
    fun getDeviceDeploymentFor_with_trigger_to_other_primary_device_succeeds()
    {
        val sourcePrimary = StubPrimaryDeviceConfiguration( "Primary 1" )
        val targetPrimary = StubPrimaryDeviceConfiguration( "Primary 2" )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( sourcePrimary )
            addPrimaryDevice( targetPrimary )
        }
        val measure = Measure.DataStream( DataType( "namespace", "type" ) )
        val task = StubTaskConfiguration( "Stub task", listOf( measure ) )
        protocol.addTaskControl( StubTriggerConfiguration( sourcePrimary ).start( task, targetPrimary ) )
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( sourcePrimary, DefaultDeviceRegistration() )
        deployment.registerDevice( targetPrimary, DefaultDeviceRegistration() )

        val sourceDeployment = deployment.getDeviceDeploymentFor( sourcePrimary )
        val targetDeployment = deployment.getDeviceDeploymentFor( targetPrimary )

        // The task should only be run on the target device.
        assertEquals( 0, sourceDeployment.tasks.size )
        assertEquals( task, targetDeployment.tasks.single() )

        // The task is triggered from the source and sent to the target.
        assertEquals( 1, sourceDeployment.triggers.size )
        assertEquals( 1, sourceDeployment.taskControls.size )
        val control = sourceDeployment.taskControls.single()
        assertEquals( task.name, control.taskName )
        assertEquals( 0, targetDeployment.triggers.size )

        // There are no connected devices, only primary devices.
        assertEquals( 0, sourceDeployment.connectedDevices.size )
        assertEquals( 0, targetDeployment.connectedDevices.size )
    }

    @Test
    fun getDeviceDeploymentFor_and_deviceDeployed_succeed_with_optional_unregistered_dependent_device()
    {
        val primary = StubPrimaryDeviceConfiguration( "Primary 1" )
        val optionalPrimary = StubPrimaryDeviceConfiguration( "Primary 2", true )
        val protocol = createEmptyProtocol().apply {
            addPrimaryDevice( primary )
            addPrimaryDevice( optionalPrimary )
        }
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, primary.createRegistration() )

        // Can get deployment.
        val deploymentStatus = deployment.getStatus()
        val deviceStatus = deploymentStatus.getDeviceStatus( primary )
        assertTrue( deviceStatus is DeviceDeploymentStatus.Registered )
        assertTrue( deviceStatus.canObtainDeviceDeployment )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        assertEquals( primary, deviceDeployment.deviceConfiguration )

        // Can complete deployment.
        deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn )
        val status = deployment.getStatus()
        assertTrue( status is StudyDeploymentStatus.Running )
    }

    @Test
    fun getDeviceDeploymentFor_fails_when_device_not_in_protocol()
    {
        val (protocol, _, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalArgumentException>
        {
            deployment.getDeviceDeploymentFor( StubPrimaryDeviceConfiguration( "Some other device" ) )
        }
    }

    @Test
    fun getDeviceDeploymentFor_fails_when_device_cant_be_deployed_yet()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalStateException> { deployment.getDeviceDeploymentFor( primary ) }
    }

    @Test
    fun deviceDeployed_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( device, device.createRegistration { } )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )
        assertTrue( deployment.deployedDevices.contains( device ) )
        assertEquals(
            StudyDeployment.Event.DeviceDeployed( device ),
            deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceDeployed>().singleOrNull()
        )
    }

    @Test
    fun deviceDeployed_for_last_device_sets_startedOn()
    {
        val protocol = createEmptyProtocol()
        val primary1 = StubPrimaryDeviceConfiguration( "Primary1" )
        val primary2 = StubPrimaryDeviceConfiguration( "Primary2" )
        protocol.addPrimaryDevice( primary1 )
        protocol.addPrimaryDevice( primary2 )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary1, primary1.createRegistration() )
        deployment.registerDevice( primary2, primary2.createRegistration() )

        // Deploying a device while others still need to be deployed does not set `startedOn`.
        val primary1Deployment = deployment.getDeviceDeploymentFor( primary1 )
        deployment.deviceDeployed( primary1, primary1Deployment.lastUpdatedOn )
        assertNull( deployment.startedOn )
        assertEquals( 0, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.Started>().count() )

        // Deploying the last device sets `startedOn`.
        val primary2Deployment = deployment.getDeviceDeploymentFor( primary2 )
        deployment.deviceDeployed( primary2, primary2Deployment.lastUpdatedOn )
        assertNotNull( deployment.startedOn )
        assertEquals(
            deployment.startedOn,
            deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.Started>().first().startedOn
        )
    }

    @Test
    fun deviceDeployed_can_be_called_multiple_times()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( device, device.createRegistration { } )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        val lastUpdatedOn = deviceDeployment.lastUpdatedOn
        deployment.deviceDeployed( device, lastUpdatedOn )
        deployment.deviceDeployed( device, lastUpdatedOn )
        assertEquals( 1, deployment.deployedDevices.count() )
        assertEquals( 1, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceDeployed>().count() )
    }

    @Test
    fun deviceDeployed_fails_for_device_not_part_of_deployment()
    {
        val deployment = createComplexDeployment()

        val invalidDevice = StubPrimaryDeviceConfiguration( "Not in deployment" )
        assertFailsWith<IllegalArgumentException> { deployment.deviceDeployed( invalidDevice, Clock.System.now() ) }
        assertEquals( 0, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceDeployed>().count() )
    }

    @Test
    fun deviceDeployed_fails_when_device_is_unregistered()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )

        assertFailsWith<IllegalStateException> { deployment.deviceDeployed( device, Clock.System.now() ) }
        assertEquals( 0, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.DeviceDeployed>().count() )
    }

    @Test
    fun deviceDeployed_fails_when_connected_device_is_unregistered()
    {
        val (protocol, primary, _) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, DefaultDeviceRegistration() )
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )

        assertFailsWith<IllegalStateException> { deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn ) }
    }

    @Test
    fun deviceDeployed_fails_with_outdated_deployment()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment: StudyDeployment = studyDeploymentFor( protocol )
        deployment.registerDevice( device, device.createRegistration { } )

        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        deployment.unregisterDevice( device )

        // Ensure new registration is more recent than previous one.
        // The timer precision on the JS runtime is sometimes not enough to notice a difference.
        // In practice, in a distributed system, the timestamps of a re-registration will never overlap due to latency.
        while ( Clock.System.now() == deviceDeployment.lastUpdatedOn ) { /* Wait. */ }
        deployment.registerDevice( device, device.createRegistration { } )

        assertFailsWith<IllegalArgumentException> { deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn ) }
    }

    @Test
    fun stop_after_ready_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( device, device.createRegistration() )
        val deviceDeployment = deployment.getDeviceDeploymentFor( device )
        deployment.deviceDeployed( device, deviceDeployment.lastUpdatedOn )

        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Running )
        assertNull( deployment.stoppedOn )

        deployment.stop()
        assertTrue( deployment.isStopped )
        assertNotNull( deployment.stoppedOn )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Stopped )
        assertEquals( 1, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.Stopped>().count() )
    }

    @Test
    fun stop_while_deploying_succeeds()
    {
        val protocol = createEmptyProtocol()
        val device = StubPrimaryDeviceConfiguration()
        protocol.addPrimaryDevice( device )
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( device, device.createRegistration() )

        assertTrue( deployment.getStatus() is StudyDeploymentStatus.DeployingDevices )
        assertNull( deployment.stoppedOn )

        deployment.stop()
        assertTrue( deployment.isStopped )
        assertNotNull( deployment.stoppedOn )
        assertTrue( deployment.getStatus() is StudyDeploymentStatus.Stopped )
        assertEquals( 1, deployment.consumeEvents().filterIsInstance<StudyDeployment.Event.Stopped>().count() )
    }

    @Test
    fun modifications_after_stop_not_allowed()
    {
        val (protocol, primary, connected) = createSinglePrimaryWithConnectedDeviceProtocol()
        val deployment = studyDeploymentFor( protocol )
        deployment.registerDevice( primary, primary.createRegistration() )
        deployment.registerDevice( connected, connected.createRegistration() )
        deployment.stop()

        assertFailsWith<IllegalStateException> { deployment.registerDevice( connected, connected.createRegistration() ) }
        assertFailsWith<IllegalStateException> { deployment.unregisterDevice( primary ) }
        val deviceDeployment = deployment.getDeviceDeploymentFor( primary )
        assertFailsWith<IllegalStateException> { deployment.deviceDeployed( primary, deviceDeployment.lastUpdatedOn ) }
    }
}
