package dk.cachet.carp.studies.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.Participant
import kotlinx.serialization.*
import kotlin.time.Instant


@Serializable
data class RecruitmentSnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val studyId: UUID,
    val studyProtocol: StudyProtocolSnapshot?,
    val invitation: StudyInvitation?,
    val participants: Set<Participant> = emptySet(),
    val participantGroups: Map<UUID, StagedParticipantGroup> = emptyMap()
) : Snapshot<Recruitment>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [recruitment] using the specified snapshot [version].
         */
        fun fromParticipantRecruitment( recruitment: Recruitment, version: Int ): RecruitmentSnapshot
        {
            val status = recruitment.getStatus()
            var studyProtocol: StudyProtocolSnapshot? = null
            var invitation: StudyInvitation? = null
            if ( status is RecruitmentStatus.ReadyForDeployment )
            {
                studyProtocol = status.studyProtocol
                invitation = status.invitation
            }
            val participations = recruitment.participantGroups

            return RecruitmentSnapshot(
                recruitment.id,
                recruitment.createdOn,
                version,
                recruitment.studyId,
                studyProtocol,
                invitation,
                recruitment.participants,
                participations
            )
        }
    }

    override fun toObject(): Recruitment = Recruitment.fromSnapshot( this )
}
