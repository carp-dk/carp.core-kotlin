package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.infrastructure.services.SingleThreadedEventBus
import dk.cachet.carp.common.test.infrastructure.TestUUIDFactory
import dk.cachet.carp.data.infrastructure.InMemoryDataStreamService
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.studies.infrastructure.InMemoryParticipantRepository
import dk.cachet.carp.studies.infrastructure.InMemoryStudyRepository
import dk.cachet.carp.test.TestClock


/**
 * Tests for [RecruitmentServiceHost].
 */
class RecruitmentServiceHostTest : RecruitmentServiceTest
{
    companion object
    {
        fun createSUT(): RecruitmentServiceTest.SUT
        {
            val eventBus = SingleThreadedEventBus()

            // Create dependent study service.
            val studyRepo = InMemoryStudyRepository()
            val studyService = StudyServiceHost(
                studyRepo,
                eventBus.createApplicationServiceAdapter( StudyService::class ),
                TestUUIDFactory(),
                TestClock
            )

            // Create dependent deployment service.
            val deploymentService = DeploymentServiceHost(
                InMemoryDeploymentRepository(),
                InMemoryDataStreamService(),
                eventBus.createApplicationServiceAdapter( DeploymentService::class ),
                TestClock
            )

            val recruitmentService = RecruitmentServiceHost(
                InMemoryParticipantRepository(),
                deploymentService,
                eventBus.createApplicationServiceAdapter( RecruitmentService::class ),
                TestUUIDFactory(),
                TestClock
            )

            return RecruitmentServiceTest.SUT( recruitmentService, studyService, deploymentService, eventBus )
        }
    }

    override fun createSUT(): RecruitmentServiceTest.SUT = RecruitmentServiceHostTest.createSUT()
}
