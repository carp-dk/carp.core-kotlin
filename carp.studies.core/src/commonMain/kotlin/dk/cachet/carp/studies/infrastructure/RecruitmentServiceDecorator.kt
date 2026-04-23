package dk.cachet.carp.studies.infrastructure

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupRepresentation


class RecruitmentServiceDecorator(
    service: RecruitmentService,
    requestDecorator: (Command<RecruitmentServiceRequest<*>>) -> Command<RecruitmentServiceRequest<*>>
) : ApplicationServiceDecorator<RecruitmentService, RecruitmentServiceRequest<*>>(
        service,
        RecruitmentServiceInvoker,
        requestDecorator
    ),
    RecruitmentService
{
    override suspend fun addParticipant( studyId: UUID, email: EmailAddress ) =
        invoke( RecruitmentServiceRequest.AddParticipantByEmailAddress( studyId, email ) )

    override suspend fun addParticipant( studyId: UUID, username: Username ): Participant =
        invoke( RecruitmentServiceRequest.AddParticipantByUsername( studyId, username ) )

    override suspend fun getParticipant( studyId: UUID, participantId: UUID ) =
        invoke( RecruitmentServiceRequest.GetParticipant( studyId, participantId ) )

    override suspend fun getParticipants( studyId: UUID ) =
        invoke( RecruitmentServiceRequest.GetParticipants( studyId ) )

    @Deprecated(
        "Use CreateParticipantGroup and InviteParticipantGroup instead",
        ReplaceWith(
            "inviteParticipantGroup( createParticipantGroup( UUID.randomUUID(), group, studyId ).id )",
            "dk.cachet.carp.common.application.UUID"
        )
    )
    @Suppress( "DEPRECATION" )
    override suspend fun inviteNewParticipantGroup(
        studyId: UUID,
        group: Set<AssignedParticipantRoles>
    ) = invoke( RecruitmentServiceRequest.InviteNewParticipantGroup( studyId, group ) )

    override suspend fun createParticipantGroup(
        groupId: UUID,
        group: Set<AssignedParticipantRoles>,
        studyId: UUID,
        representation: ParticipantGroupRepresentation
    ) = invoke( RecruitmentServiceRequest.CreateParticipantGroup( groupId, group, studyId, representation ) )

    override suspend fun updateParticipantGroup(
        groupId: UUID,
        group: Set<AssignedParticipantRoles>?,
        representation: ParticipantGroupRepresentation?
    ) = invoke( RecruitmentServiceRequest.UpdateParticipantGroup( groupId, group, representation ) )

    override suspend fun inviteParticipantGroup( groupId: UUID ) =
        invoke( RecruitmentServiceRequest.InviteParticipantGroup( groupId ) )

    override suspend fun getParticipantGroupStatusList( studyId: UUID ) =
        invoke( RecruitmentServiceRequest.GetParticipantGroupStatusList( studyId ) )

    override suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ) =
        invoke( RecruitmentServiceRequest.StopParticipantGroup( studyId, groupId ) )
}


object RecruitmentServiceInvoker : ApplicationServiceInvoker<RecruitmentService, RecruitmentServiceRequest<*>>
{
    @Suppress( "DEPRECATION" )
    override suspend fun RecruitmentServiceRequest<*>.invoke( service: RecruitmentService ): Any =
        when ( this )
        {
            is RecruitmentServiceRequest.AddParticipantByEmailAddress -> service.addParticipant( studyId, email )
            is RecruitmentServiceRequest.AddParticipantByUsername -> service.addParticipant( studyId, username )
            is RecruitmentServiceRequest.GetParticipant -> service.getParticipant( studyId, participantId )
            is RecruitmentServiceRequest.GetParticipants -> service.getParticipants( studyId )
            is RecruitmentServiceRequest.InviteNewParticipantGroup ->
                service.inviteNewParticipantGroup( studyId, group )
            is RecruitmentServiceRequest.CreateParticipantGroup ->
                service.createParticipantGroup( groupId, group, studyId, representation )
            is RecruitmentServiceRequest.UpdateParticipantGroup ->
                service.updateParticipantGroup( groupId, group, representation )
            is RecruitmentServiceRequest.InviteParticipantGroup -> service.inviteParticipantGroup( groupId )
            is RecruitmentServiceRequest.GetParticipantGroupStatusList ->
                service.getParticipantGroupStatusList( studyId )
            is RecruitmentServiceRequest.StopParticipantGroup -> service.stopParticipantGroup( studyId, groupId )
        }
}
