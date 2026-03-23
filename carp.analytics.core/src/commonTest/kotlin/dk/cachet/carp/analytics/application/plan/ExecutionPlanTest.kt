package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Tests for [ExecutionPlan] validation and serialization.
 *
 * Verifies:
 * - Validation of required fields
 * - Issue tracking and severity handling
 * - Serialization round-trips
 * - Environment reference tracking
 */
class ExecutionPlanTest
{
    /**
     * Helper to create a test PlannedStep.
     */
    private fun step( id: UUID ): PlannedStep =
        PlannedStep(
            metadata = StepMetadata(
                id = id,
                name = "Step $id"
            ),
            process = InTasksRun( operationId = "op-$id" ),
            bindings = ResolvedBindings(),
            environmentRef = UUID.randomUUID()
        )

    @Test
    fun `validate passes for minimal valid plan`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = emptyList(),
            requiredEnvironmentRefs = emptyMap()
        )

        plan.validate() // should not throw
    }

    @Test
    fun `validate rejects blank workflowId`()
    {
        val plan = ExecutionPlan(
            workflowId = " ",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) )
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `validate rejects blank planId`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = " ",
            steps = listOf( step( UUID.randomUUID() ) )
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
            steps = listOf( step( stepId ), step( stepId ) )
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `validate rejects empty steps list`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = emptyList()
        )
        assertFailsWith<IllegalArgumentException> { plan.validate() }
    }

    @Test
    fun `hasErrors true when any ERROR issue exists`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = listOf(
                PlanIssue( severity = PlanIssueSeverity.INFO, code = "I", message = "info" ),
                PlanIssue( severity = PlanIssueSeverity.ERROR, code = "E", message = "bad" ),
            )
        )

        assertTrue( plan.hasErrors() )
        assertFalse( plan.isRunnable() )
    }

    @Test
    fun `hasErrors false when only INFO and WARNING issues exist`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = listOf(
                PlanIssue( severity = PlanIssueSeverity.INFO, code = "I", message = "info" ),
                PlanIssue( severity = PlanIssueSeverity.WARNING, code = "W", message = "warn" ),
            )
        )

        assertFalse( plan.hasErrors() )
        assertTrue( plan.isRunnable() )
    }

    @Test
    fun `isRunnable true when no errors and steps not empty`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = emptyList()
        )

        assertTrue( plan.isRunnable() )
    }

    @Test
    fun `plan with no issues has empty issues list`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) )
        )

        assertTrue( plan.issues.isEmpty() )
    }

    @Test
    fun `round-trip serialization preserves all fields`()
    {
        val stepId = UUID.randomUUID()
        val environmentId = UUID.randomUUID()
        val issueStepId = UUID.randomUUID()

        val plan = ExecutionPlan(
            workflowId = "test-workflow",
            planId = "plan-123",
            steps = listOf( step( stepId ) ),
            issues = listOf(
                PlanIssue(
                    severity = PlanIssueSeverity.WARNING,
                    code = "W1",
                    message = "Test warning",
                    stepId = issueStepId
                )
            ),
            requiredEnvironmentRefs = mapOf(
                environmentId to CondaEnvironmentRef(
                    id = environmentId.toString(),
                    name = "test-env",
                    dependencies = listOf( "numpy", "scipy" ),
                    channels = listOf( "conda-forge" ),
                    pythonVersion = "3.11"
                )
            )
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( plan )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<ExecutionPlan>( encoded )

        assertEquals( plan, decoded )
        assertEquals( "test-workflow", decoded.workflowId )
        assertEquals( "plan-123", decoded.planId )
        assertEquals( 1, decoded.steps.size )
        assertEquals( 1, decoded.issues.size )
        assertEquals( 1, decoded.requiredEnvironmentRefs.size )
    }

    @Test
    fun `plan issue with null stepId is preserved`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = listOf(
                PlanIssue(
                    severity = PlanIssueSeverity.WARNING,
                    code = "W1",
                    message = "Plan-level warning",
                    stepId = null  // Not tied to a specific step
                )
            )
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( plan )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<ExecutionPlan>( encoded )

        assertEquals( 1, decoded.issues.size )
        assertNull( decoded.issues[0].stepId )
    }

    @Test
    fun `multiple issues are preserved in order`()
    {
        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            issues = listOf(
                PlanIssue( severity = PlanIssueSeverity.INFO, code = "I1", message = "Info 1" ),
                PlanIssue( severity = PlanIssueSeverity.WARNING, code = "W1", message = "Warning 1" ),
                PlanIssue( severity = PlanIssueSeverity.INFO, code = "I2", message = "Info 2" ),
            )
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( plan )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<ExecutionPlan>( encoded )

        assertEquals( 3, decoded.issues.size )
        assertEquals( "I1", decoded.issues[0].code )
        assertEquals( "W1", decoded.issues[1].code )
        assertEquals( "I2", decoded.issues[2].code )
    }

    @Test
    fun `multiple steps are preserved`()
    {
        val stepIds = List( 5 ) { UUID.randomUUID() }
        val steps = stepIds.map { step( it ) }

        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = steps
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( plan )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<ExecutionPlan>( encoded )

        assertEquals( 5, decoded.steps.size )
        assertEquals( stepIds, decoded.steps.map { it.metadata.id } )
    }

    @Test
    fun `multiple environment refs are preserved`()
    {
        val env1Id = UUID.randomUUID()
        val env2Id = UUID.randomUUID()

        val environments = mapOf(
            env1Id to CondaEnvironmentRef(
                id = env1Id.toString(),
                name = "env-1",
                dependencies = listOf( "numpy" ),
                channels = listOf( "conda-forge" ),
                pythonVersion = "3.11"
            ),
            env2Id to CondaEnvironmentRef(
                id = env2Id.toString(),
                name = "env-2",
                dependencies = listOf( "scipy" ),
                channels = listOf( "conda-forge" ),
                pythonVersion = "3.12"
            )
        )

        val plan = ExecutionPlan(
            workflowId = "wf",
            planId = "plan",
            steps = listOf( step( UUID.randomUUID() ) ),
            requiredEnvironmentRefs = environments
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( plan )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<ExecutionPlan>( encoded )

        assertEquals( 2, decoded.requiredEnvironmentRefs.size )
        assertTrue( decoded.requiredEnvironmentRefs.containsKey( env1Id ) )
        assertTrue( decoded.requiredEnvironmentRefs.containsKey( env2Id ) )
    }
}