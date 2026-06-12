package dk.cachet.carp.deployments

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.DeviceDeploymentStatus
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ParticipantInvitation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Instant


class DeploymentCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember", "UNUSED_VARIABLE" )
    fun readme() = runTest {
        val deploymentService: DeploymentService = createDeploymentEndpoint()
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val patientPhone: Smartphone = trackPatientStudy.primaryDevices.first() as Smartphone // "Patient's phone"

        // This is called by `StudyService` when deploying a participant group.
        val invitation = ParticipantInvitation(
            participantId = UUID.randomUUID(),
            assignedRoles = AssignedTo.All,
            identity = AccountIdentity.fromEmailAddress( "test@test.com" ),
            invitation = StudyInvitation( "Movement study", "This study tracks your movements." )
        )
        val studyDeploymentId = UUID.randomUUID()
        deploymentService.createStudyDeployment(
            studyDeploymentId,
            trackPatientStudy.getSnapshot(),
            listOf( invitation )
        )

        // What comes after is similar to what is called by the client in `carp.client`:
        // - Register the device to be deployed.
        val registration = patientPhone.createRegistration()
        var status = deploymentService.registerDevice( studyDeploymentId, patientPhone.roleName, registration )

        // - Retrieve information on what to run and indicate the device is ready to collect the requested data.
        val patientPhoneStatus: DeviceDeploymentStatus = status.getDeviceStatus( patientPhone )
        if ( patientPhoneStatus.canObtainDeviceDeployment ) // True since there are no dependent devices.
        {
            val deploymentInformation: PrimaryDeviceDeployment =
                deploymentService.getDeviceDeploymentFor( studyDeploymentId, patientPhone.roleName )
            val deployedOn: Instant = deploymentInformation.lastUpdatedOn // To verify correct deployment.
            deploymentService.deviceDeployed( studyDeploymentId, patientPhone.roleName, deployedOn )
        }

        // Now that all devices have been registered and deployed, the deployment is running.
        status = deploymentService.getStudyDeploymentStatus( studyDeploymentId )
        val isReady = status is StudyDeploymentStatus.Running // True.
    }


    /**
     * This is the protocol created in ProtocolsCodeSamples.readme().
     */
    private fun createExampleProtocol(): StudyProtocol
    {
        val ownerId = UUID.randomUUID()
        val protocol = StudyProtocol( ownerId, "Track patient movement" )

        val phone = Smartphone( "Patient's phone" )
        protocol.addPrimaryDevice( phone )

        val sensors = Smartphone.Sensors
        val trackMovement = Smartphone.Tasks.BACKGROUND.create( "Track movement" ) {
            measures = listOf( sensors.GEOLOCATION.measure(), sensors.STEP_COUNT.measure() )
        }
        protocol.addTaskControl( phone.atStartOfStudy().start( trackMovement, phone ) )

        return protocol
    }

    private val eventBus: EventBus = SingleThreadedEventBus()
    private fun createDeploymentEndpoint(): DeploymentService = DeploymentServiceHost(
        InMemoryDeploymentRepository(),
        InMemoryDataStreamService(),
        eventBus.createApplicationServiceAdapter( DeploymentService::class )
    )
}
