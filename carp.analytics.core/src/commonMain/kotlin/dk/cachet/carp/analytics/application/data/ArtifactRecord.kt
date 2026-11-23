package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import kotlin.time.Instant


/**
 * Whether an artefact describes the whole cohort or one participant.
 */
@Serializable
enum class ArtifactScope
{
    COHORT,
    PARTICIPANT
}


/**
 * An artefact produced by a run, and where to fetch it.
 *
 * @property stepId The step that produced the artefact.
 * @property outputId The step output the artefact was written to.
 * @property producedAt When the artefact was recorded. Clients can pass the latest value they have seen
 *   to `ExecutionService.getParticipantResults` to get only newer artefacts.
 * @property subjectId The participant the artefact describes, as named by its data source. Set only for [ArtifactScope.PARTICIPANT].
 * @property studyDeploymentId The CARP study deployment of [subjectId]. Null when the subject does not come from CARP data,
 *   in which case the artefact is not visible to participants.
 */
@Serializable
data class ArtifactRecord(
    val stepId: UUID,
    val outputId: UUID,
    val reference: ArtifactReference,
    val producedAt: Instant,
    val scope: ArtifactScope = ArtifactScope.COHORT,
    val subjectId: String? = null,
    val studyDeploymentId: UUID? = null
)
{
    init
    {
        require( scope == ArtifactScope.PARTICIPANT || (subjectId == null && studyDeploymentId == null) )
            { "Only participant-scoped artifacts name a subject." }
        require( scope == ArtifactScope.COHORT || subjectId != null )
            { "A participant-scoped artifact needs a subject." }
    }
}
