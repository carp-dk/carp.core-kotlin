package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.test.infrastructure.versioning.OutputTestRequests
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest
import dk.cachet.carp.studies.application.RecruitmentServiceTest
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest


class OutputRecruitmentServiceTestRequests :
    OutputTestRequests<RecruitmentService, RecruitmentService.Event, RecruitmentServiceRequest<*>>(
        RecruitmentService::class,
        ::RecruitmentServiceDecorator
    ),
    RecruitmentServiceTest
{
    override fun createSUT(): RecruitmentServiceTest.SUT
    {
        val sut = RecruitmentServiceHostTest.createSUT()
        val loggedService = createLoggedApplicationService( sut.recruitmentService, sut.eventBus )

        return RecruitmentServiceTest.SUT( loggedService, sut.studyService, sut.deploymentService, sut.eventBus )
    }
}
