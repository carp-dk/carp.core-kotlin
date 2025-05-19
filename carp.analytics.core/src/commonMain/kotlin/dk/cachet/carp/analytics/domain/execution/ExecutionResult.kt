package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.data.OutputDataReference
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Represents the result of a completed workflow execution.
 */
@Serializable
sealed interface ExecutionResult {
    val executionId: UUID
    val status: ExecutionStatus
    val outputs: List<OutputDataReference>?
}

/**
 * Describes an artifact produced by workflow execution, such as a file, report, or visualization.
 */
@Serializable
data class ExecutionArtifact(
    val uri: String,                        // Download or API-accessible URI
    val name: String,                       // Display name (e.g., "Heart Rate Plot")
    val type: ArtifactType,                // Enum for file, image, etc.
    val mimeType: String? = null            // Optional MIME type (e.g., application/pdf)
)

@Serializable
data class BasicExecutionResult(
    override val executionId: UUID,
    override val status: ExecutionStatus,
    override val outputs: List<OutputDataReference>,
    val artifacts: List<ExecutionArtifact> = emptyList()
) : ExecutionResult
