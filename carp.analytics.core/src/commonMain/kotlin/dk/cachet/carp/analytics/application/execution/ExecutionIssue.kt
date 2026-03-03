package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A run-level issue recorded during execution, distinct from per-step failures.
 *
 * Examples: workspace creation errors, policy violations, unexpected orchestrator state.
 *
 * @param stepId  Associated step, or null for run-level issues.
 * @param kind    Categorizes the issue.
 * @param message Human-readable description.
 */
@Serializable
data class ExecutionIssue(
    val stepId: UUID? = null,
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
    UNKNOWN
}
