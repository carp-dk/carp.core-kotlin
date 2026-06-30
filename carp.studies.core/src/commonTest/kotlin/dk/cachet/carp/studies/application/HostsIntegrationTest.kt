package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.deployments.infrastructure.InMemoryParticipationRepository
import dk.cachet.carp.protocols.infrastructure.test.createSinglePrimaryDeviceProtocol
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*


/**
 * Tests whether different application service hosts are correctly integrated through integration events.
 */
class HostsIntegrationTest
{
    lateinit var eventBus: EventBus

    lateinit var studyService: StudyService
    lateinit var recruitmentService: RecruitmentService

    lateinit var deploymentService: DeploymentService
    lateinit var participationService: ParticipationService

    @BeforeTest
    fun startServices()
    {
        eventBus = SingleThreadedEventBus()

        // Create study service.
        val studyRepo = InMemoryStudyRepository()
        studyService = StudyServiceHost( studyRepo, eventBus.createApplicationServiceAdapter( StudyService::class ) )

        // Create deployment service.
        deploymentService = DeploymentServiceHost(
            InMemoryDeploymentRepository(),
            InMemoryDataStreamService(),
            eventBus.createApplicationServiceAdapter( DeploymentService::class )
        )

        // Create dependent participation service.
        val accountService = InMemoryAccountService()
        participationService = ParticipationServiceHost(
            InMemoryParticipationRepository(),
            ParticipantGroupService( accountService ),
            eventBus.createApplicationServiceAdapter( ParticipationService::class )
        )

        recruitmentService = RecruitmentServiceHost(
            InMemoryParticipantRepository(),
            deploymentService,
            eventBus.createApplicationServiceAdapter( RecruitmentService::class )
        )
    }


    @Test
    fun create_study_creates_recruitment() = runTest {
        var studyCreated: StudyService.Event.StudyCreated? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyCreated::class, this ) { studyCreated = it }
        eventBus.activateHandlers( this )

        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val participants = recruitmentService.getParticipants( study.studyId )

        assertEquals( study.studyId, studyCreated?.study?.studyId )
        assertEquals( 0, participants.size )
    }

    @Test
    fun when_study_goes_live_recruitment_is_ready_for_deployment() = runTest {
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId
        val protocol = createSinglePrimaryDeviceProtocol( "Device" )
        studyService.setProtocol( studyId, protocol.getSnapshot() )

        var studyGoneLive: StudyService.Event.StudyGoneLive? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyGoneLive::class, this ) { studyGoneLive = it }
        eventBus.activateHandlers( this )
        studyService.goLive( studyId )
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )

        // Call succeeding means recruitment is ready for deployment.
        val assignRoles = setOf( AssignedParticipantRoles( participant.id, AssignedTo.All ) )
        val group = recruitmentService.createParticipantGroup(
            groupId = UUID.randomUUID(),
            group = assignRoles,
            study.studyId
        )
        recruitmentService.inviteParticipantGroup( group.id )

        assertEquals( study.studyId, studyGoneLive?.study?.studyId )
    }

    @Test
    fun remove_study_removes_recruitment_and_deployment() = runTest {
        val (studyId, _) = createLiveStudy()

        // Add participant and deploy participant group.
        val participant = recruitmentService.addParticipant( studyId, EmailAddress( "test@test.com" ) )
        val assignRoles = AssignedParticipantRoles( participant.id, AssignedTo.All )
        val group = recruitmentService.createParticipantGroup(
            groupId = UUID.randomUUID(),
            group = setOf( assignRoles ),
            studyId
        )
        recruitmentService.inviteParticipantGroup( group.id )

        var studyRemovedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyRemoved::class, this ) { studyRemovedEvent = it }
        var deploymentRemovedEvent: DeploymentService.Event.StudyDeploymentRemoved? = null
        eventBus.registerHandler( DeploymentService::class, DeploymentService.Event.StudyDeploymentRemoved::class, this )
            { deploymentRemovedEvent = it }
        eventBus.activateHandlers( this )
        studyService.remove( studyId )

        assertEquals( studyId, studyRemovedEvent?.studyId )
        assertEquals( group.id, deploymentRemovedEvent?.studyDeploymentId )

         // Data related to study no longer exists.
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipantGroupStatusList( studyId ) }
        assertFailsWith<IllegalArgumentException> { recruitmentService.getParticipants( studyId ) }
        assertFailsWith<IllegalArgumentException> { deploymentService.getStudyDeploymentStatus( group.id ) }
        assertFailsWith<IllegalArgumentException> { participationService.getParticipantData( group.id ) }
    }

    @Test
    fun remove_study_does_not_trigger_event_when_study_does_not_exist() = runTest {
        var removedEvent: StudyService.Event.StudyRemoved? = null
        eventBus.registerHandler( StudyService::class, StudyService.Event.StudyRemoved::class, this ) { removedEvent = it }
        eventBus.activateHandlers( this )
        studyService.remove( UUID.randomUUID() )

        assertNull( removedEvent )
    }


    /**
     * Create a live study with a protocol containing one device.
     */
    private suspend fun createLiveStudy(): Pair<UUID, String>
    {
        val study = studyService.createStudy( UUID.randomUUID(), "Test" )
        val studyId = study.studyId
        val deviceRole = "Phone"
        val protocol = createSinglePrimaryDeviceProtocol( deviceRole )
        studyService.setProtocol( studyId, protocol.getSnapshot() )
        studyService.goLive( studyId )

        return Pair( studyId, deviceRole )
    }
}
