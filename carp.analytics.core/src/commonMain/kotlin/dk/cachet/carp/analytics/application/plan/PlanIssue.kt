package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PlanIssue(
    val severity: PlanIssueSeverity,
    val code: String,
    val message: String,
    // Optional: which step the issue refers to. Null means plan-level issue.
    val stepId: UUID? = null,
)
{
    init
    {
        require(code.isNotBlank())
        {
            "code must not be blank."
        }
        require(message.isNotBlank())
        {
            "message must not be blank."
        }
    }
}

@Serializable
enum class PlanIssueSeverity
{
    INFO,
    WARNING,
    ERROR,
}
