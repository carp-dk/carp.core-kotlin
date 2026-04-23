package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.DependentServices
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupRepresentation
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import kotlinx.serialization.*


/**
 * Application service which allows setting recruitment goals,
 * adding participants to studies, and creating deployments for them.
 */
@DependentServices( StudyService::class )
interface RecruitmentService : ApplicationService<RecruitmentService, RecruitmentService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 3 ) }

    @Serializable
    sealed class Event : IntegrationEvent<RecruitmentService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [email] address.
     * In case the [email] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun addParticipant( studyId: UUID, email: EmailAddress ): Participant

    /**
     * Add a [Participant] to the study with the specified [studyId], identified by the specified [username].
     * In case the [username] was already added before, the same [Participant] is returned.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun addParticipant( studyId: UUID, username: Username ): Participant

    /**
     * Returns a participant of a study with the specified [studyId], identified by [participantId].
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant with [participantId] does not exist.
     */
    suspend fun getParticipant( studyId: UUID, participantId: UUID ): Participant

    /**
     * Get all [Participant]s for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getParticipants( studyId: UUID ): List<Participant>

    /**
     * Create a new participant [group] of previously added participants and instantly send out invitations
     * to participate in the study with the given [studyId].
     *
     * In case a group with the same participants has already been deployed and is still running (not stopped),
     * the latest status for this group is simply returned.
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - [group] is empty
     *  - any of the participant roles specified in [group] does not exist
     *  - not all necessary participant roles part of the study have been assigned a participant
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    @Deprecated(
        "Use createParticipantGroup and inviteParticipantGroup instead",
        ReplaceWith(
            "inviteParticipantGroup( createParticipantGroup( UUID.randomUUID(), group, studyId ).id )",
            "dk.cachet.carp.common.application.UUID"
        ),
        level = DeprecationLevel.WARNING
    )
    suspend fun inviteNewParticipantGroup( studyId: UUID, group: Set<AssignedParticipantRoles> ): ParticipantGroupStatus

    /**
     * Create a new participant [group] of previously added participants for the study with the given [studyId],
     * and a [representation] representing this group, but do not yet send out invitations.
     * This is used to create a group of participants which can be deployed at a later time.
     *
     * [ParticipantGroupRepresentation.Default] is used when no [representation] is passed.
     *
     * As long as no final study protocol is locked in for the study, a participant group can't be created
     * since participant roles to which participants need to be assigned are unknown.
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - an existing participant group with [groupId] already exists
     *  - any of the participant roles specified in [group] does not exist
     * @throws IllegalStateException when the study is not yet ready for deployment.
     */
    suspend fun createParticipantGroup(
        groupId: UUID,
        group: Set<AssignedParticipantRoles>,
        studyId: UUID,
        representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
    ): ParticipantGroupStatus

    /**
     * Update the participant group for the specified [groupId].
     *
     * Participant assignments can't be changed after the group has been invited; the group representation can always
     * be updated.
     *
     * @param group If set, role assignments are updated; unchanged otherwise.
     * @param representation If set, group representation is updated; unchanged otherwise.
     * @throws IllegalArgumentException when:
     *  - the participant group with [groupId] does not exist
     *  - any of the participant roles specified in [group] does not exist
     *  - any of the participants specified in [group] does not exist
     * @throws IllegalStateException when the group has already been deployed and [group] changes participant assignments.
     */
    suspend fun updateParticipantGroup(
        groupId: UUID,
        group: Set<AssignedParticipantRoles>? = null,
        representation: ParticipantGroupRepresentation? = null
    ): ParticipantGroupStatus

    /**
     * Invite the participant group with the specified [groupId] to start participating in its study.
     *
     * @throws IllegalArgumentException when:
     *  - the participant group with [groupId] does not exist
     *  - not all necessary participant roles part of the study have been assigned a participant
     * @throws IllegalStateException when group has already been deployed.
     */
    suspend fun inviteParticipantGroup( groupId: UUID ): ParticipantGroupStatus

    /**
     * Get the status of all participant groups in the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getParticipantGroupStatusList( studyId: UUID ): List<ParticipantGroupStatus>

    /**
     * Stop the study deployment in the study with the given [studyId]
     * of the participant group with the specified [groupId] (equivalent to the studyDeploymentId).
     * No further changes to this deployment will be allowed and no more data will be collected.
     *
     * @throws IllegalArgumentException when a study with [studyId] or participant group with [groupId] does not exist.
     */
    suspend fun stopParticipantGroup( studyId: UUID, groupId: UUID ): ParticipantGroupStatus
}
