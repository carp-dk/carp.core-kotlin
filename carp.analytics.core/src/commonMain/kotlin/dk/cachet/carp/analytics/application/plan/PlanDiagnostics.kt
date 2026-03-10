package dk.cachet.carp.analytics.application.plan

import kotlinx.serialization.Serializable

@Serializable
data class PlanDiagnostics(
    val planId: String, // Which plan
    val workflowId: String, // From which workflow
    val stepCount: Int, // How many steps
)

object PlanDiagnosticsBuilder
{
    fun build(plan: ExecutionPlan): PlanDiagnostics = PlanDiagnostics(
        planId = TODO(),
        workflowId = TODO(),
        stepCount = TODO()
    )
}
