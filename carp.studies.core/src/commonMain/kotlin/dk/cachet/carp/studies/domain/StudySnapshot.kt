package dk.cachet.carp.studies.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StudySnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val ownerId: UUID,
    val name: String,
    val description: String? = null,
    val invitation: StudyInvitation,
    val versionedProtocolSnapshot: VersionedStudyProtocolSnapshot?,
    val isLive: Boolean
) : Snapshot<Study>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [Study] using the specified snapshot [version].
         */
        fun fromStudy( study: Study, version: Int ): StudySnapshot
        {
            return StudySnapshot(
                id = study.id,
                createdOn = study.createdOn,
                version = version,
                ownerId = study.ownerId,
                name = study.name,
                description = study.description,
                invitation = study.invitation,
                versionedProtocolSnapshot = study.versionedProtocolSnapshot,
                isLive = study.isLive
            )
        }
    }

    override fun toObject(): Study = Study.fromSnapshot( this )
}
