package dk.cachet.carp.analytics.domain.validation

import kotlin.test.*

/**
 * Test suite for [WorkflowValidator].
 *
 * This test suite validates the behavior of the generic workflow validator using a simple test workflow model.
 */
class WorkflowValidatorTest
{

    // Simple test models for use in tests
    data class TestStep(
        val id: String,
        val dependsOn: List<String> = emptyList()
    )

    data class TestWorkflow(
        val id: String,
        val steps: List<TestStep>
    )

    // Helper function to create a default config
    private fun createConfig(): WorkflowValidator.Config<TestWorkflow, TestStep>
    {
        return WorkflowValidator.Config(
            workflowId = { it.id },
            steps = { it.steps },
            stepId = { it.id },
            dependencies = { it.dependsOn },
            hasDependencyGraph = true
        )
    }

    // ============ Tests: Unique Step IDs ============

    @Test
    fun `validate returns OK when workflow has no steps`()
    {
        val workflow = TestWorkflow( id = "wf1", steps = emptyList() )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate returns OK when all step IDs are unique`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2" ),
                TestStep( id = "s3" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects single duplicate step ID`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s1" ),
                TestStep( id = "s2" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationSeverity.ERROR, issue.severity )
        assertEquals( ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE, issue.code )
        assertTrue( issue.message.contains( "s1" ) )
        assertTrue( issue.path!!.contains( "steps" ) )
    }

    @Test
    fun `validate detects multiple duplicate step IDs`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s1" ),
                TestStep( id = "s2" ),
                TestStep( id = "s2" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 2, result.issues.size )

        val codes = result.issues.map { it.code }
        assertTrue( codes.all { it == ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE } )
    }

    @Test
    fun `duplicate step ID issue includes correct workflow ID`()
    {
        val workflow = TestWorkflow(
            id = "myWorkflow",
            steps = listOf(
                TestStep( id = "duplicate" ),
                TestStep( id = "duplicate" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( 1, result.issues.size )
        val issue = result.issues[0]
        assertEquals( "myWorkflow", issue.subjectId )
        assertTrue( issue.message.contains( "myWorkflow" ) )
    }

    // ============ Tests: Dependency References ============

    @Test
    fun `validate returns OK when all dependencies reference existing steps`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = emptyList() ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s1", "s2" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects single missing dependency reference`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2", dependsOn = listOf( "missing" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationSeverity.ERROR, issue.severity )
        assertEquals( ValidationErrorCode.WORKFLOW_DEP_REFERENCE_MISSING, issue.code )
        assertTrue( issue.message.contains( "missing" ) )
        assertTrue( issue.message.contains( "s2" ) )
    }

    @Test
    fun `validate detects multiple missing dependencies in same step`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2", dependsOn = listOf( "missing1", "missing2" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 2, result.issues.size )

        val codes = result.issues.map { it.code }
        assertTrue( codes.all { it == ValidationErrorCode.WORKFLOW_DEP_REFERENCE_MISSING } )
    }

    @Test
    fun `validate detects missing dependencies across multiple steps`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "missing1" ) ),
                TestStep( id = "s2", dependsOn = listOf( "missing2" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s1", "s2" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 2, result.issues.size )

        val messages = result.issues.map { it.message }
        assertTrue( messages.any { it.contains( "missing1" ) } )
        assertTrue( messages.any { it.contains( "missing2" ) } )
    }

    @Test
    fun `dependency reference issue includes correct step ID`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2", dependsOn = listOf( "nonexistent" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( 1, result.issues.size )
        assertTrue( result.issues[0].message.contains( "s2" ) )
        assertTrue( result.issues[0].message.contains( "nonexistent" ) )
    }

    // ============ Tests: Cycle Detection ============

    @Test
    fun `validate returns OK when no cycles exist`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = emptyList() ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s4", dependsOn = listOf( "s2", "s3" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects self-dependency cycle`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s1" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED, issue.code )
        assertTrue( issue.message.contains( "cycle" ) )
    }

    @Test
    fun `validate detects two-step cycle`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED, issue.code )
        assertTrue( issue.message.contains( "cycle" ) )
    }

    @Test
    fun `validate detects three-step cycle`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s2", dependsOn = listOf( "s3" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s1" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED, issue.code )
        assertTrue( issue.message.contains( "s1" ) || issue.message.contains( "s2" ) || issue.message.contains( "s3" ) )
    }

    @Test
    fun `validate detects multiple cycles`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s4" ) ),
                TestStep( id = "s4", dependsOn = listOf( "s3" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertEquals( 2, result.issues.size )

        val codes = result.issues.map { it.code }
        assertTrue( codes.all { it == ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED } )
    }

    @Test
    fun `cycle issue includes correct workflow ID`()
    {
        val workflow = TestWorkflow(
            id = "cycleWorkflow",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s1" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( 1, result.issues.size )
        val issue = result.issues[0]
        assertEquals( "cycleWorkflow", issue.subjectId )
        assertTrue( issue.message.contains( "cycleWorkflow" ) )
    }

    // ============ Tests: hasDependencyGraph Flag ============

    @Test
    fun `validate with hasDependencyGraph=false skips dependency and cycle checks`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "missing" ) ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s2" ) // duplicate step ID still caught
            )
        )
        val config = WorkflowValidator.Config<TestWorkflow, TestStep>(
            workflowId = { it.id },
            steps = { it.steps },
            stepId = { it.id },
            dependencies = { it.dependsOn },
            hasDependencyGraph = false
        )
        val result = WorkflowValidator.validate( workflow, config )

        // Should only catch duplicate step ID, not missing dependency
        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )
        assertEquals( ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE, result.issues[0].code )
    }

    @Test
    fun `validate with hasDependencyGraph=false passes even with cycles`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) )
            )
        )
        val config = WorkflowValidator.Config<TestWorkflow, TestStep>(
            workflowId = { it.id },
            steps = { it.steps },
            stepId = { it.id },
            dependencies = { it.dependsOn },
            hasDependencyGraph = false
        )
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    // ============ Tests: Combined Issues ============

    @Test
    fun `validate reports all issue types together`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "s1" ) ), // cycle: self-dependency
                TestStep( id = "s2", dependsOn = listOf( "s3" ) ), // missing reference
                TestStep( id = "s3", dependsOn = listOf( "s2" ) ), // cycle: two-step cycle
                TestStep( id = "s3", dependsOn = listOf( "s4" ) ) // duplicate ID
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertFalse( result.isValid )
        assertTrue( result.issues.size >= 3 )

        val codes = result.issues.map { it.code }
        assertTrue( codes.contains( ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE ) )
        assertTrue( codes.contains( ValidationErrorCode.WORKFLOW_DEP_REFERENCE_MISSING ) )
        assertTrue( codes.contains( ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED ) )
    }

    @Test
    fun `all issues have correct severity`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "missing" ) ),
                TestStep( id = "s1" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.issues.all { it.severity == ValidationSeverity.ERROR } )
    }

    // ============ Tests: Custom Config ============

    @Test
    fun `validate uses custom workflowPath when provided`()
    {
        val workflow = TestWorkflow(
            id = "myWf",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s1" )
            )
        )
        val config = WorkflowValidator.Config<TestWorkflow, TestStep>(
            workflowId = { it.id },
            steps = { it.steps },
            stepId = { it.id },
            workflowPath = { "custom.path[${it.id}]" }
        )
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( 1, result.issues.size )
        assertTrue( result.issues[0].path!!.contains( "custom.path[myWf]" ) )
    }

    @Test
    fun `validate uses custom stepPath when provided`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2", dependsOn = listOf( "missing" ) )
            )
        )
        val config = WorkflowValidator.Config<TestWorkflow, TestStep>(
            workflowId = { it.id },
            steps = { it.steps },
            stepId = { it.id },
            dependencies = { it.dependsOn },
            stepPath = { _, s -> "custom.steps[${s.id}]" }
        )
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( 1, result.issues.size )
        assertTrue( result.issues[0].path!!.contains( "custom.steps[s2]" ) )
    }

    @Test
    fun `validate works with custom step models`()
    {
        data class CustomStep( val name: String, val deps: List<String> )
        data class CustomWorkflow( val title: String, val allSteps: List<CustomStep> )

        val workflow = CustomWorkflow(
            title = "custom",
            allSteps = listOf(
                CustomStep( name = "task1", deps = emptyList() ),
                CustomStep( name = "task2", deps = listOf( "task1" ) )
            )
        )
        val config = WorkflowValidator.Config<CustomWorkflow, CustomStep>(
            workflowId = { it.title },
            steps = { it.allSteps },
            stepId = { it.name },
            dependencies = { it.deps }
        )
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    // ============ Tests: Edge Cases ============

    @Test
    fun `validate with single step returns OK`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf( TestStep( id = "s1" ) )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate handles steps with no dependencies`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2" ),
                TestStep( id = "s3" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate handles linear dependency chain`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = emptyList() ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s2" ) ),
                TestStep( id = "s4", dependsOn = listOf( "s3" ) ),
                TestStep( id = "s5", dependsOn = listOf( "s4" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate handles complex DAG with multiple paths`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1", dependsOn = emptyList() ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s3", dependsOn = listOf( "s1" ) ),
                TestStep( id = "s4", dependsOn = listOf( "s2", "s3" ) ),
                TestStep( id = "s5", dependsOn = listOf( "s4" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate handles many steps`()
    {
        val manySteps = ( 1..100 ).map { i -> TestStep( id = "s$i", dependsOn = if ( i == 1 ) emptyList() else listOf( "s${i - 1}" ) ) }
        val workflow = TestWorkflow( id = "wf1", steps = manySteps )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    // ============ Tests: ValidationResult Integration ============

    @Test
    fun `invalid workflow returns non-OK ValidationResult`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "dup" ),
                TestStep( id = "dup" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertNotEquals( result, ValidationResult.OK )
    }

    @Test
    fun `valid workflow returns OK ValidationResult`()
    {
        val workflow = TestWorkflow(
            id = "wf1",
            steps = listOf(
                TestStep( id = "s1" ),
                TestStep( id = "s2", dependsOn = listOf( "s1" ) )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        assertEquals( result, ValidationResult.OK )
    }

    @Test
    fun `returned issues can be examined for details`()
    {
        val workflow = TestWorkflow(
            id = "testWf",
            steps = listOf(
                TestStep( id = "s1", dependsOn = listOf( "missing" ) ),
                TestStep( id = "s1" )
            )
        )
        val config = createConfig()
        val result = WorkflowValidator.validate( workflow, config )

        val idIssue = result.issues.find { it.code == ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE }
        val refIssue = result.issues.find { it.code == ValidationErrorCode.WORKFLOW_DEP_REFERENCE_MISSING }

        assertNotNull( idIssue )
        assertNotNull( refIssue )
        assertEquals( "testWf", idIssue.subjectId )
        assertEquals( "testWf", refIssue.subjectId )
    }
}
