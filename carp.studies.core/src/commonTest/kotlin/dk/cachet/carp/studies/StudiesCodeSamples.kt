package dk.cachet.carp.studies

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.Smartphone
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.start
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*


class StudiesCodeSamples
{
    @Test
    @Suppress( "UnusedPrivateMember", "UNUSED_VARIABLE" )
    fun readme() = runTest {
        val (studyService, recruitmentService) = createEndpoints()

        // Create a new study.
        val ownerId = UUID.randomUUID()
        var studyStatus: StudyStatus = studyService.createStudy( ownerId, "Example study" )
        val studyId: UUID = studyStatus.studyId

        // Let the study use the protocol from the 'carp.protocols' example above.
        val trackPatientStudy: StudyProtocol = createExampleProtocol()
        val protocolSnapshot: StudyProtocolSnapshot = trackPatientStudy.getSnapshot()
        studyStatus = studyService.setProtocol( studyId, protocolSnapshot )

        // Add a participant.
        val email = EmailAddress( "participant@email.com" )
        val participant: Participant = recruitmentService.addParticipant( studyId, email )

        // Once all necessary study options have been configured, the study can go live.
        if ( studyStatus is StudyStatus.Configuring && studyStatus.canGoLive )
        {
            studyStatus = studyService.goLive( studyId )
        }

        // Once the study is live, you can 'deploy' it to participant's devices. They will be invited.
        if ( studyStatus.canDeployToParticipants )
        {
            // Create a 'participant group' with a single participant; `AssignedTo.All` assigns the "Patient's phone".
            val participation = AssignedParticipantRoles( participant.id, AssignedTo.All )
            val participantGroup = setOf( participation )

            val groupId = UUID.randomUUID()
            var groupStatus: ParticipantGroupStatus =
                recruitmentService.createParticipantGroup( groupId, participantGroup, studyId )
            val isStaged = groupStatus is ParticipantGroupStatus.Staged // True.

            groupStatus = recruitmentService.inviteParticipantGroup( groupId )
            val isInvited = groupStatus is ParticipantGroupStatus.Invited // True.
        }
    }


    private fun createEndpoints(): Pair<StudyService, RecruitmentService>
    {
        val eventBus = SingleThreadedEventBus()

        val studyRepo = InMemoryStudyRepository()
        val studyService = StudyServiceHost(
            studyRepo,
            eventBus.createApplicationServiceAdapter( StudyService::class )
        )

        val deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            InMemoryDataStreamService(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class )
        )

        val recruitmentService = RecruitmentServiceHost(
            InMemoryParticipantRepository(),
            deploymentService,
            eventBus.createApplicationServiceAdapter( RecruitmentService::class )
        )

        return Pair( studyService, recruitmentService )
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
}
