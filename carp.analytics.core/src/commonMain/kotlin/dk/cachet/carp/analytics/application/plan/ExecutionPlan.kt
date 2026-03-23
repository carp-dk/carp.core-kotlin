package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A planned, executable description of a workflow run.
 *
 * - Contains no runtime state and no environment instances.
 * - Produced by a planner; consumed by an engine.
 */
@Serializable
data class ExecutionPlan(
    val workflowId: String,
    val planId: String,
    val steps: List<PlannedStep>,
    val issues: List<PlanIssue> = emptyList(),
    val requiredEnvironmentRefs: Map<UUID, EnvironmentRef> = emptyMap()
)
{
    fun validate()
    {
        require(workflowId.isNotBlank())
        {
            "workflowId must not be blank."
        }
        require(planId.isNotBlank())
        {
            "planId must not be blank."
        }
        require(steps.isNotEmpty())
        {
            "steps must not be empty."
        }

        // ensuring stepIds are unique
        val ids = steps.map { it.metadata.id }
        require(ids.size == ids.distinct().size) {
            "steps contains duplicate stepMetadata(s): ${ids.groupBy { it }.filterValues { it.size > 1 }.keys}"
        }
    }

    fun hasErrors(): Boolean = issues.any { it.severity == PlanIssueSeverity.ERROR }

    /**
     * Runnable means: planner produced no ERROR issues.
     * (Warnings/Info do not block.)
     */
    fun isRunnable(): Boolean = !hasErrors()

    fun diagnostics( hasher: PlanHasher ): PlanDiagnostics =
        PlanDiagnosticsBuilder.build( this, hasher )
}
