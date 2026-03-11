package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlin.test.*

class PlanDiagnosticsBuilderTest
{
    private val mockHasher = MockPlanHasher("test-hash-value")

    @Test
    fun diagnostics_includes_all_plan_info()
    {
        val plan = createTestExecutionPlan(stepCount = 2, environmentCount = 1)

        val diags = PlanDiagnosticsBuilder.build(plan, mockHasher)

        assertEquals(plan.planId, diags.planId)
        assertEquals(2, diags.stepCount)
        assertEquals(1, diags.environmentCount)
        assertTrue(diags.planHash.isNotEmpty())
    }

    @Test
    fun diagnostics_counts_issues_by_severity()
    {
        val plan = createTestPlanWithIssues(errors = 1, warnings = 2, infos = 3)

        val diags = PlanDiagnosticsBuilder.build(plan, mockHasher)

        assertEquals(1, diags.issueSummary.errorCount)
        assertEquals(2, diags.issueSummary.warningCount)
        assertEquals(3, diags.issueSummary.infoCount)
    }

    @Test
    fun diagnostics_valid_when_no_errors()
    {
        val plan = createTestExecutionPlan(issues = emptyList())

        val diags = PlanDiagnosticsBuilder.build(plan, mockHasher)

        assertTrue(diags.isValid)
    }

    @Test
    fun diagnostics_invalid_when_has_errors()
    {
        val plan = createTestPlanWithIssues(errors = 1)

        val diags = PlanDiagnosticsBuilder.build(plan, mockHasher)

        assertFalse(diags.isValid)
    }

    @Test
    fun diagnostics_includes_step_summaries()
    {
        val plan = createTestExecutionPlan(stepCount = 3)

        val diags = PlanDiagnosticsBuilder.build(plan, mockHasher)

        assertEquals(3, diags.stepSummaries.size)
        diags.stepSummaries.forEach { summary ->
            assertNotNull(summary.stepId)
            assertNotNull(summary.name)
        }
    }

    fun createTestExecutionPlan(
        workflowId: String = "test-workflow",
        planId: String = UUID.randomUUID().toString(),
        stepCount: Int = 2,
        environmentCount: Int = 1,
        issues: List<PlanIssue> = emptyList()
    ): ExecutionPlan
    {
        val steps = (1..stepCount).map { i ->
            PlannedStep(
                stepId = UUID.randomUUID(),
                name = "Step $i",
                process = CommandSpec("python", listOf(ExpandedArg.Literal("script.py"))),
                bindings = ResolvedBindings(
                    inputs = mapOf(),
                    outputs = mapOf(UUID.randomUUID() to DataRef(UUID.randomUUID(), "csv"))
                ),
                environmentRef = UUID.randomUUID()
            )
        }

        val environments = (1..environmentCount).associate { i ->
            UUID.randomUUID() to CondaEnvironmentRef(
                id = "env-$i",
                name = "test-env-$i",
                dependencies = listOf("numpy"),
                channels = listOf("conda-forge"),
                pythonVersion = "3.11"
            )
        }

        return ExecutionPlan(
            workflowId = workflowId,
            planId = planId,
            steps = steps,
            issues = issues,
            requiredEnvironmentRefs = environments
        )
    }

// ── PlanIssue Helpers ──────────────────────────────────────────────────────────

    fun createTestPlanWithIssues(
        errors: Int = 0,
        warnings: Int = 0,
        infos: Int = 0
    ): ExecutionPlan
    {
        val issues = mutableListOf<PlanIssue>()

        repeat(errors) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.ERROR,
                    code = "ERROR_$i",
                    message = "Error $i",
                    stepId = null
                )
            )
        }

        repeat(warnings) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.WARNING,
                    code = "WARNING_$i",
                    message = "Warning $i",
                    stepId = null
                )
            )
        }

        repeat(infos) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.INFO,
                    code = "INFO_$i",
                    message = "Info $i",
                    stepId = null
                )
            )
        }

        return createTestExecutionPlan(issues = issues)
    }

// ── Mock Objects ───────────────────────────────────────────────────────────────

    class MockPlanHasher( val hashValue: String = "mock-hash-12345" ) : PlanHasher
    {
        override fun hash( plan: ExecutionPlan ): String = hashValue
    }
}
