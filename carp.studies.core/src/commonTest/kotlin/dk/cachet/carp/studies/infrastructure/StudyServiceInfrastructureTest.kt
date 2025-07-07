package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceDecoratorTest
import dk.cachet.carp.common.test.infrastructure.ApplicationServiceRequestsTest
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHostTest


class StudyServiceRequestsTest : ApplicationServiceRequestsTest<StudyService, StudyServiceRequest<*>>(
    ::StudyServiceDecorator,
    StudyServiceRequest.Serializer,
    REQUESTS
)
{
    companion object
    {
        private val studyId = UUID.randomUUID()

        val REQUESTS: List<StudyServiceRequest<*>> = listOf(
            StudyServiceRequest.CreateStudy( UUID.randomUUID(), "Test", "Description", StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetInternalDescription( studyId, "New name", "New description" ),
            StudyServiceRequest.GetStudyDetails( studyId ),
            StudyServiceRequest.GetStudyStatus( studyId ),
            StudyServiceRequest.GetStudiesOverview( UUID.randomUUID() ),
            StudyServiceRequest.SetInvitation( studyId, StudyInvitation( "Some study" ) ),
            StudyServiceRequest.SetProtocol(
                studyId,
                VersionedStudyProtocolSnapshot(
                    StudyProtocol( UUID.randomUUID(), "Test" ).getSnapshot(),
                    ProtocolVersion( "Version 1" )
                )
            ),
            StudyServiceRequest.RemoveProtocol( studyId ),
            StudyServiceRequest.GoLive( studyId ),
            StudyServiceRequest.Remove( studyId )
        )
    }


    override fun createService() = StudyServiceHostTest.createService().first
}


class StudyServiceDecoratorTest :
    ApplicationServiceDecoratorTest<StudyService, StudyService.Event, StudyServiceRequest<*>>(
        StudyServiceRequestsTest(),
        StudyServiceInvoker
    )
