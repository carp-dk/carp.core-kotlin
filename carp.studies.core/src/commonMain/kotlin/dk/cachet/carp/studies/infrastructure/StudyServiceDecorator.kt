package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyService


@Suppress( "TooManyFunctions" )
class StudyServiceDecorator(
    service: StudyService,
    requestDecorator: (Command<StudyServiceRequest<*>>) -> Command<StudyServiceRequest<*>>
) : ApplicationServiceDecorator<StudyService, StudyServiceRequest<*>>( service, StudyServiceInvoker, requestDecorator ),
    StudyService
{
    override suspend fun createStudy(
        ownerId: UUID,
        name: String,
        description: String?,
        invitation: StudyInvitation?
    ) = invoke( StudyServiceRequest.CreateStudy( ownerId, name, description, invitation ) )

    override suspend fun setInternalDescription(
        studyId: UUID,
        name: String,
        description: String?
    ) = invoke( StudyServiceRequest.SetInternalDescription( studyId, name, description ) )

    override suspend fun getStudyDetails( studyId: UUID ) = invoke( StudyServiceRequest.GetStudyDetails( studyId ) )

    override suspend fun getStudyStatus( studyId: UUID ) = invoke( StudyServiceRequest.GetStudyStatus( studyId ) )

    override suspend fun getStudiesOverview( ownerId: UUID ) =
        invoke( StudyServiceRequest.GetStudiesOverview( ownerId ) )

    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ) =
        invoke( StudyServiceRequest.SetInvitation( studyId, invitation ) )

    override suspend fun setProtocol( studyId: UUID, protocol: VersionedStudyProtocolSnapshot ) =
        invoke( StudyServiceRequest.SetProtocol( studyId, protocol ) )

    override suspend fun removeProtocol( studyId: UUID ) = invoke( StudyServiceRequest.RemoveProtocol( studyId ) )

    override suspend fun goLive( studyId: UUID ) = invoke( StudyServiceRequest.GoLive( studyId ) )

    override suspend fun remove( studyId: UUID ) = invoke( StudyServiceRequest.Remove( studyId ) )
}


object StudyServiceInvoker : ApplicationServiceInvoker<StudyService, StudyServiceRequest<*>>
{
    override suspend fun StudyServiceRequest<*>.invoke( service: StudyService ): Any? =
        when ( this )
        {
            is StudyServiceRequest.CreateStudy -> service.createStudy( ownerId, name, description, invitation )
            is StudyServiceRequest.SetInternalDescription ->
                service.setInternalDescription( studyId, name, description )
            is StudyServiceRequest.GetStudyDetails -> service.getStudyDetails( studyId )
            is StudyServiceRequest.GetStudyStatus -> service.getStudyStatus( studyId )
            is StudyServiceRequest.GetStudiesOverview -> service.getStudiesOverview( ownerId )
            is StudyServiceRequest.SetInvitation -> service.setInvitation( studyId, invitation )
            is StudyServiceRequest.SetProtocol -> service.setProtocol( studyId, protocol )
            is StudyServiceRequest.RemoveProtocol -> service.removeProtocol( studyId )
            is StudyServiceRequest.GoLive -> service.goLive( studyId )
            is StudyServiceRequest.Remove -> service.remove( studyId )
        }
}
