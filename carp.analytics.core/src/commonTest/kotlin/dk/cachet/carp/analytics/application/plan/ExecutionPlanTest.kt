package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.infrastructure.serialization.DspSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

class ExecutionPlanTest
{

    private fun step( id:  UUID): PlannedStep =
        PlannedStep(
            stepId = id,
            name = "Step $id",
            process = InTasksRun(operationId = "op-$id"),
            bindings = ResolvedBindings(),
            environmentDefinitionId = UUID.randomUUID()
        )

    @Test
    fun `validate passes for minimal valid plan`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID())),
            issues = emptyList(),
            requiredEnvironmentHandles = emptyList()
        )

        plan.validate() // should not throw
    }

    @Test
    fun `validate rejects blank workflowId`()
    {
        val plan = ExecutionPlan(
            workflowId = " ",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID()))
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `validate rejects blank planId`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = " ",
            steps = listOf(step(UUID.randomUUID()))
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `validate rejects duplicate stepIds`()
    {
        val stepId = UUID.randomUUID()
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
                steps = listOf(step(stepId), step(stepId))
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `validate rejects duplicate environment handle ids`()
    {
        val uuid = UUID.randomUUID()
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID())),
            requiredEnvironmentHandles = listOf(
                uuid,
                uuid,
            )
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `hasErrors true when any ERROR issue exists`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID())),
            issues = listOf(
                PlanIssue(severity = PlanIssueSeverity.INFO, code = "I", message = "info"),
                PlanIssue(severity = PlanIssueSeverity.ERROR, code = "E", message = "bad"),
            )
        )

        assertTrue(plan.hasErrors())
        assertFalse(plan.isRunnable())
    }

    @Test
    fun `hasErrors false when only INFO and WARNING issues exist`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID())),
            issues = listOf(
                PlanIssue(severity = PlanIssueSeverity.INFO, code = "I", message = "info"),
                PlanIssue(severity = PlanIssueSeverity.WARNING, code = "W", message = "warn"),
            )
        )

        assertFalse(plan.hasErrors())
        assertTrue(plan.isRunnable())
    }

    @Test
    fun `round-trip serialization`()
    {
        val uuid = UUID.randomUUID()
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf(step(UUID.randomUUID())),
            issues = listOf(
                PlanIssue(severity = PlanIssueSeverity.WARNING, code = "W1", message = "warn", stepId = UUID.randomUUID())
            ),
            requiredEnvironmentHandles = listOf(uuid)
        )

        val encoded = DspSerializer.json.encodeToString(plan)
        val decoded = DspSerializer.json.decodeFromString<ExecutionPlan>(encoded)

        assertEquals(plan, decoded)
    }
}
