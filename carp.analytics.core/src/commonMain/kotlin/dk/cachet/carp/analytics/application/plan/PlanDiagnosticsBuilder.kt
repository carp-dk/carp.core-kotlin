package dk.cachet.carp.analytics.application.plan

import kotlin.time.Clock


object PlanDiagnosticsBuilder
{

    fun build( plan: ExecutionPlan, hasher: PlanHasher ): PlanDiagnostics
    {
        // Count issues by severity
        val errorCount = plan.issues.count { it.severity == PlanIssueSeverity.ERROR }
        val warningCount = plan.issues.count { it.severity == PlanIssueSeverity.WARNING }
        val infoCount = plan.issues.count { it.severity == PlanIssueSeverity.INFO }

        // Create step summaries
        val stepSummaries = plan.steps.map { step ->
            PlannedStepSummary(
                metadata = step.metadata,
                environmentId = step.environmentRef,
                inputCount = step.bindings.inputs.size,
                outputCount = step.bindings.outputs.size
            )
        }

        // Compute plan hash
        val planHash = hasher.hash(plan)

        // Determine validity
        val isValid = !plan.hasErrors()

        // Build diagnostics
        return PlanDiagnostics(
            planId = plan.planId,
            workflowId = plan.workflowName,
            timestamp = Clock.System.now().toString(),
            stepCount = plan.steps.size,
            environmentCount = plan.requiredEnvironmentRefs.size,
            bindingCount = plan.steps.sumOf { it.bindings.inputs.size + it.bindings.outputs.size },
            executionOrder = plan.steps.map { "${it.metadata.name} (${it.metadata.id})" },
            issueSummary = IssueSummary(errorCount, warningCount, infoCount),
            issues = plan.issues,
            stepSummaries = stepSummaries,
            planHash = planHash,
            isValid = isValid
        )
    }
}
