package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceDecoratorTest
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHostTest


class RecruitmentServiceRequestsTest : ApplicationServiceRequestsTest<RecruitmentService, RecruitmentServiceRequest<*>>(
    ::RecruitmentServiceDecorator,
    RecruitmentServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<RecruitmentServiceRequest<*>> = listOf(
            RecruitmentServiceRequest.AddParticipantByEmailAddress( studyId, EmailAddress( "test@test.com" ) ),
            RecruitmentServiceRequest.AddParticipantByUsername( studyId, Username( "test" ) ),
            RecruitmentServiceRequest.GetParticipant( studyId, UUID.randomUUID() ),
            RecruitmentServiceRequest.GetParticipants( studyId ),
            @Suppress( "DEPRECATION" )
            RecruitmentServiceRequest.InviteNewParticipantGroup( studyId, setOf(), "test group" ),
            RecruitmentServiceRequest.CreateParticipantGroup( UUID.randomUUID(), setOf(), studyId, "test group" ),
            RecruitmentServiceRequest.UpdateParticipantGroup( UUID.randomUUID(), setOf(), "updated group" ),
            RecruitmentServiceRequest.InviteParticipantGroup( UUID.randomUUID() ),
            RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ),
            RecruitmentServiceRequest.StopParticipantGroup( studyId, UUID.randomUUID() )
        )
    }


    override fun createService() = RecruitmentServiceHostTest.createSUT().recruitmentService
}


class RecruitmentServiceDecoratorTest :
    ApplicationServiceDecoratorTest<RecruitmentService, RecruitmentService.Event, RecruitmentServiceRequest<*>>(
        RecruitmentServiceRequestsTest(),
        RecruitmentServiceInvoker
    )
