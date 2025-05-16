package dk.cachet.carp.analytics.domain.execution

import kotlinx.serialization.Serializable

@Serializable
enum class ArtifactType {
    FILE,
    IMAGE,
    PLOT,
    REPORT,
    ARCHIVE
}
