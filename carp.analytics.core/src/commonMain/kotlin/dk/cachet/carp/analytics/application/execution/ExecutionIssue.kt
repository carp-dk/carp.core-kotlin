package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import kotlinx.serialization.Serializable

/**
 * A run-level issue recorded during execution, distinct from per-step failures.
 *
 * Examples: workspace creation errors, policy violations, unexpected orchestrator state.
 *
 * @param stepMetadata  Associated step metadata, or null for run-level issues.
 * @param kind    Categorizes the issue.
 * @param message Human-readable description.
 */
@Serializable
data class ExecutionIssue(
    val stepMetadata: StepMetadata? = null,
    val kind: ExecutionIssueKind,
    val message: String
)

@Serializable
enum class ExecutionIssueKind
{
    WORKSPACE_ERROR,
    POLICY_VIOLATION,
    ORCHESTRATOR_ERROR,
    OUTPUT_MISSING,
    UNEXPECTED_OUTPUT,
    ARTIFACT_COLLECTION_FAILED,
    PROCESS_FAILED,
    UNKNOWN
}
