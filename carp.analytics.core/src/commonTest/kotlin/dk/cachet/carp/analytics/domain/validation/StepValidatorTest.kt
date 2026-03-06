package dk.cachet.carp.analytics.domain.validation

import kotlin.test.*

/**
 * Test suite for [StepValidator].
 *
 * This test suite validates the behaviour of the generic step validator using a simple test step model.
 */
class StepValidatorTest
{

    // Simple test step model for use in tests
    data class TestStep(
        val id: String,
        val outputs: List<String>,
        val missingRequiredInputs: List<String> = emptyList(),
        val specsWithoutTypes: List<String> = emptyList()
    )

    // Helper function to create a default config
    private fun createConfig(): StepValidator.Config<TestStep>
    {
        return StepValidator.Config(
            stepId = { it.id },
            outputIds = { it.outputs },
            missingRequiredInputSpecs = { it.missingRequiredInputs },
            specsMissingTypes = { it.specsWithoutTypes }
        )
    }

    // ============ Tests: Unique Output IDs ============

    @Test
    fun `validate returns OK when step has no outputs`()
    {
        val step = TestStep( id = "s1", outputs = emptyList() )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate returns OK when all output IDs are unique`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1", "out2", "out3" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects single duplicate output ID`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1", "out1", "out2" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationSeverity.ERROR, issue.severity )
        assertEquals( ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID, issue.code )
        assertEquals( "s1", issue.subjectId )
        assertTrue( issue.message.contains( "out1" ) )
        assertTrue( issue.path!!.contains( "outputs" ) )
    }

    @Test
    fun `validate detects multiple duplicate output IDs`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1", "out1", "out2", "out2" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 2, result.issues.size )

        assertTrue( result.issues.all { it.code == ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID } )
    }

    @Test
    fun `duplicate output issue includes correct path and step ID`()
    {
        val step = TestStep( id = "my-step", outputs = listOf( "duplicate", "duplicate" ) )
        val config = StepValidator.Config<TestStep>(
            stepId = { it.id },
            outputIds = { it.outputs },
            stepPath = { "custom.path[${it.id}]" }
        )
        val result = StepValidator.validate( step, config )

        assertEquals( 1, result.issues.size )
        val issue = result.issues[0]
        assertEquals( "custom.path[my-step].outputs", issue.path )
        assertEquals( "my-step", issue.subjectId )
        assertTrue( issue.message.contains( "my-step" ) )
    }

    // ============ Tests: Required Input Specs ============

    @Test
    fun `validate returns OK when no required inputs are missing`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1" ), missingRequiredInputs = emptyList() )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects single missing required input`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1" ), missingRequiredInputs = listOf( "input1" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationSeverity.ERROR, issue.severity )
        assertEquals( ValidationErrorCode.STEP_INPUT_REQUIRED_MISSING, issue.code )
        assertTrue( issue.message.contains( "input1" ) )
        assertTrue( issue.path!!.contains( "inputs" ) )
    }

    @Test
    fun `validate detects multiple missing required inputs`()
    {
        val step = TestStep(
            id = "s1",
            outputs = listOf( "out1" ),
            missingRequiredInputs = listOf( "input1", "input2", "input3" )
        )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 3, result.issues.size )

        assertTrue( result.issues.all { it.code == ValidationErrorCode.STEP_INPUT_REQUIRED_MISSING } )
    }

    // ============ Tests: Spec Type Presence ============

    @Test
    fun `validate returns OK when all specs have types`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1" ), specsWithoutTypes = emptyList() )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate detects single spec missing type`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "out1" ), specsWithoutTypes = listOf( "input1" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )

        val issue = result.issues[0]
        assertEquals( ValidationSeverity.ERROR, issue.severity )
        assertEquals( ValidationErrorCode.STEP_SPEC_TYPE_MISSING, issue.code )
        assertTrue( issue.message.contains( "input1" ) )
        assertTrue( issue.path!!.contains( "specs" ) )
    }

    @Test
    fun `validate detects multiple specs missing types`()
    {
        val step = TestStep(
            id = "s1",
            outputs = listOf( "out1" ),
            specsWithoutTypes = listOf( "input1", "output2", "config3" )
        )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 3, result.issues.size )

        assertTrue( result.issues.all { it.code == ValidationErrorCode.STEP_SPEC_TYPE_MISSING } )
    }

    // ============ Tests: Combined Issues ============

    @Test
    fun `validate reports all issues types together`()
    {
        val step = TestStep(
            id = "s1",
            outputs = listOf( "out1", "out1", "out2" ),
            missingRequiredInputs = listOf( "input1" ),
            specsWithoutTypes = listOf( "spec1" )
        )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 3, result.issues.size )

        val codes = result.issues.map { it.code }
        assertTrue( codes.contains( ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID ) )
        assertTrue( codes.contains( ValidationErrorCode.STEP_INPUT_REQUIRED_MISSING ) )
        assertTrue( codes.contains( ValidationErrorCode.STEP_SPEC_TYPE_MISSING ) )
    }

    @Test
    fun `all issues have correct step ID and severity`()
    {
        val step = TestStep(
            id = "my-step",
            outputs = listOf( "out1", "out1" ),
            missingRequiredInputs = listOf( "input1" ),
            specsWithoutTypes = listOf( "spec1" )
        )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertEquals( 3, result.issues.size )
        assertTrue( result.issues.all { it.subjectId == "my-step" } )
        assertTrue( result.issues.all { it.severity == ValidationSeverity.ERROR } )
    }

    // ============ Tests: Custom Config ============

    @Test
    fun `validate uses custom stepPath when provided`()
    {
        val step = TestStep( id = "my-step", outputs = listOf( "out1", "out1" ) )
        val config = StepValidator.Config<TestStep>(
            stepId = { it.id },
            outputIds = { it.outputs },
            stepPath = { s -> "workflow.steps.${s.id}" }
        )
        val result = StepValidator.validate( step, config )

        assertEquals( 1, result.issues.size )
        assertTrue( result.issues[0].path!!.contains( "workflow.steps.my-step" ) )
    }

    @Test
    fun `validate works with custom extractors`()
    {
        data class CustomStep( val name: String, val outputs: List<String> )

        val step = CustomStep( name = "custom", outputs = listOf( "out1", "out2", "out1" ) )
        val config = StepValidator.Config<CustomStep>(
            stepId = { it.name },
            outputIds = { it.outputs }
        )
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 1, result.issues.size )
        assertEquals( ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID, result.issues[0].code )
        assertEquals( "custom", result.issues[0].subjectId )
    }

    // ============ Tests: Edge Cases ============

    @Test
    fun `validate with single output returns OK`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "single" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate handles empty string IDs`()
    {
        val step = TestStep( id = "", outputs = listOf( "" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        // Empty IDs are valid from validator perspective (content validation is elsewhere)
        assertTrue( result.isValid )
    }

    @Test
    fun `validate handles many outputs without duplicates`()
    {
        val manyOutputs = (1..100).map { "out$it" }
        val step = TestStep( id = "s1", outputs = manyOutputs )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertTrue( result.isValid )
        assertEquals( 0, result.issues.size )
    }

    @Test
    fun `validate with many duplicate outputs`()
    {
        val manyOutputs = listOf( "out1", "out1", "out2", "out2", "out3", "out3" )
        val step = TestStep( id = "s1", outputs = manyOutputs )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertFalse( result.isValid )
        assertEquals( 3, result.issues.size )
        assertTrue( result.issues.all { it.code == ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID } )
    }

    // ============ Tests: ValidationResult Integration ============

    @Test
    fun `invalid step returns non-OK ValidationResult`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "dup", "dup" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertNotEquals( result, ValidationResult.OK )
    }

    @Test
    fun `valid step returns OK ValidationResult`()
    {
        val step = TestStep( id = "s1", outputs = listOf( "unique1", "unique2" ) )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        assertEquals( result, ValidationResult.OK )
    }

    @Test
    fun `returned issues can be examined for details`()
    {
        val step = TestStep(
            id = "testStep",
            outputs = listOf( "x", "x" ),
            missingRequiredInputs = listOf( "required" )
        )
        val config = createConfig()
        val result = StepValidator.validate( step, config )

        val outputIssue = result.issues.find { it.code == ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID }
        val inputIssue = result.issues.find { it.code == ValidationErrorCode.STEP_INPUT_REQUIRED_MISSING }

        assertNotNull( outputIssue )
        assertNotNull( inputIssue )
        assertEquals( "testStep", outputIssue.subjectId )
        assertEquals( "testStep", inputIssue.subjectId )
    }
}
