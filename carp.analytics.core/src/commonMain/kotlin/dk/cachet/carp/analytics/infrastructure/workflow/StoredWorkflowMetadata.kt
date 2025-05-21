package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class StoredWorkflowMetadata(
    val id: UUID,
    val studyId: UUID,
    val name: String,
    val description: String?,
    val versionMajor: Int,
    val versionMinor: Int?,
    val filePath: String,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    fun toWorkflowMetadata(): WorkflowMetadata =
        WorkflowMetadata(
            id = id,
            name = name,
            description = description,
            version = Version(
                major = versionMajor,
                minor = versionMinor
            )
        )

    companion object {
        fun fromMetadata(metadata: WorkflowMetadata, studyId: UUID, filePath: String): StoredWorkflowMetadata =
            StoredWorkflowMetadata(
                id = metadata.id,
                studyId = studyId,
                name = metadata.name,
                description = metadata.description,
                versionMajor = metadata.version.major,
                versionMinor = metadata.version.minor,
                filePath = filePath
            )
    }
}
