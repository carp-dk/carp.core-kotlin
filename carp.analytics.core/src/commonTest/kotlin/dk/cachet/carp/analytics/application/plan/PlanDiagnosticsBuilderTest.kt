package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.FileLocation
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Tests for [PlanDiagnosticsBuilder].
 *
 * Verifies:
 * - Diagnostics includes all plan information
 * - Issue counts by severity
 * - Validity determination
 * - Step summaries
 * - Plan hashing
 */
class PlanDiagnosticsBuilderTest
{
    private val mockHasher = MockPlanHasher( "test-hash-value" )

    @Test
    fun `diagnostics includes all plan info`()
    {
        val plan = createTestExecutionPlan( stepCount = 2, environmentCount = 1 )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertEquals( plan.planId, diags.planId )
        assertEquals( 2, diags.stepCount )
        assertEquals( 1, diags.environmentCount )
        assertTrue( diags.planHash.isNotEmpty() )
        assertEquals( "test-hash-value", diags.planHash )
    }

    @Test
    fun `diagnostics counts issues by severity`()
    {
        val plan = createTestPlanWithIssues( errors = 1, warnings = 2, infos = 3 )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertEquals( 1, diags.issueSummary.errorCount )
        assertEquals( 2, diags.issueSummary.warningCount )
        assertEquals( 3, diags.issueSummary.infoCount )
    }

    @Test
    fun `diagnostics valid when no errors`()
    {
        val plan = createTestExecutionPlan( issues = emptyList() )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertTrue( diags.isValid )
    }

    @Test
    fun `diagnostics invalid when has errors`()
    {
        val plan = createTestPlanWithIssues( errors = 1 )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertFalse( diags.isValid )
    }

    @Test
    fun `diagnostics includes step summaries`()
    {
        val plan = createTestExecutionPlan( stepCount = 3 )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertEquals( 3, diags.stepSummaries.size )
        diags.stepSummaries.forEach { summary ->
            assertNotNull( summary.metadata.id )
            assertNotNull( summary.metadata.name )
        }
    }

    @Test
    fun `diagnostics with no issues has empty issue list`()
    {
        val plan = createTestExecutionPlan( issues = emptyList() )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertEquals( diags.issueSummary.errorCount, 0 )
        assertEquals( diags.issueSummary.warningCount, 0 )
        assertEquals( diags.issueSummary.infoCount, 0 )
    }

    @Test
    fun `diagnostics with many issues counts correctly`()
    {
        val plan = createTestPlanWithIssues( errors = 5, warnings = 10, infos = 15 )

        val diags = PlanDiagnosticsBuilder.build( plan, mockHasher )

        assertEquals( 5, diags.issueSummary.errorCount )
        assertEquals( 10, diags.issueSummary.warningCount )
        assertEquals( 15, diags.issueSummary.infoCount )
        assertEquals( 3, diags.stepCount + diags.environmentCount )
    }

    @Test
    fun `diagnostics with different hasher produces different hash`()
    {
        val plan = createTestExecutionPlan( stepCount = 1 )

        val diags1 = PlanDiagnosticsBuilder.build( plan, MockPlanHasher( "hash-1" ) )
        val diags2 = PlanDiagnosticsBuilder.build( plan, MockPlanHasher( "hash-2" ) )

        assertEquals( "hash-1", diags1.planHash )
        assertEquals( "hash-2", diags2.planHash )
        assertNotEquals( diags1.planHash, diags2.planHash )
    }


    // Test Helpers


    /**
     * Create a test execution plan with configurable parameters.
     * Uses unified DataLocation model.
     */
    private fun createTestExecutionPlan(
        workflowId: String = "test-workflow",
        planId: String = UUID.randomUUID().toString(),
        stepCount: Int = 2,
        environmentCount: Int = 1,
        issues: List<PlanIssue> = emptyList()
    ): ExecutionPlan
    {
        val steps = ( 1..stepCount ).map { i ->
            val stepId = UUID.randomUUID()
            val outputId = UUID.randomUUID()

            val outputBinding = ResolvedOutput(
                spec = OutputDataSpec(
                    id = outputId,
                    name = "output-$i",
                    location = FileLocation( path = "", format = FileFormat.CSV )
                ),
                location = FileLocation(
                    path = "/workspace/output/output-$i.csv",
                    format = FileFormat.CSV
                )
            )

            PlannedStep(
                metadata = StepMetadata(
                    id = stepId,
                    name = "Step $i",
                    version = Version( 1 )
                ),
                process = CommandSpec( "python", listOf( ExpandedArg.Literal( "script.py" ) ) ),
                bindings = ResolvedBindings(
                    inputs = emptyMap(),
                    outputs = mapOf( outputId to outputBinding )
                ),
                environmentRef = UUID.randomUUID()
            )
        }

        val environments = ( 1..environmentCount ).associate { i ->
            val envId = UUID.randomUUID()
            envId to CondaEnvironmentRef(
                id = envId.toString(),
                name = "test-env-$i",
                dependencies = listOf( "numpy" ),
                channels = listOf( "conda-forge" ),
                pythonVersion = "3.11"
            )
        }

        return ExecutionPlan(
            workflowName = workflowId,
            planId = planId,
            steps = steps,
            issues = issues,
            requiredEnvironmentRefs = environments
        )
    }

    /**
     * Create a test execution plan with specific issue counts.
     */
    private fun createTestPlanWithIssues(
        errors: Int = 0,
        warnings: Int = 0,
        infos: Int = 0
    ): ExecutionPlan
    {
        val issues = mutableListOf<PlanIssue>()

        repeat( errors ) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.ERROR,
                    code = "ERROR_$i",
                    message = "Error $i",
                    stepId = null
                )
            )
        }

        repeat( warnings ) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.WARNING,
                    code = "WARNING_$i",
                    message = "Warning $i",
                    stepId = null
                )
            )
        }

        repeat( infos ) { i ->
            issues.add(
                PlanIssue(
                    severity = PlanIssueSeverity.INFO,
                    code = "INFO_$i",
                    message = "Info $i",
                    stepId = null
                )
            )
        }

        return createTestExecutionPlan( issues = issues )
    }


    // Mock Objects


    /**
     * Mock PlanHasher for testing.
     *
     * Returns a fixed hash value for deterministic testing.
     */
    class MockPlanHasher( val hashValue: String = "mock-hash-12345" ) : PlanHasher
    {
        override fun hash( plan: ExecutionPlan ): String = hashValue
    }
}
