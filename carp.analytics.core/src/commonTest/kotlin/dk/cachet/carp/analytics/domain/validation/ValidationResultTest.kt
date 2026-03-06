package dk.cachet.carp.analytics.domain.validation

import kotlin.test.*

class ValidationResultTest
{

    @Test
    fun `isValid returns true when no issues exist`()
    {
        val result = ValidationResult.OK
        assertTrue( result.isValid )
    }

    @Test
    fun `isValid returns true when only INFO and WARNING issues exist`()
    {
        val issues = listOf(
            ValidationIssue(
                severity = ValidationSeverity.INFO,
                code = ValidationErrorCode.MISSING_METADATA,
                message = "This is an info message"
            ),
            ValidationIssue(
                severity = ValidationSeverity.WARNING,
                code = ValidationErrorCode.NAMING_CONVENTION_VIOLATION,
                message = "This is a warning message"
            )
        )
        val result = ValidationResult( issues )
        assertTrue( result.isValid )
    }

    @Test
    fun `isValid returns false when ERROR severity issue exists`()
    {
        val issues = listOf(
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE,
                message = "This is an error message"
            )
        )
        val result = ValidationResult( issues )
        assertFalse( result.isValid )
    }

    @Test
    fun `isValid returns false when multiple issues include at least one ERROR`()
    {
        val issues = listOf(
            ValidationIssue(
                severity = ValidationSeverity.INFO,
                code = ValidationErrorCode.MISSING_METADATA,
                message = "Info message"
            ),
            ValidationIssue(
                severity = ValidationSeverity.WARNING,
                code = ValidationErrorCode.NAMING_CONVENTION_VIOLATION,
                message = "Warning message"
            ),
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE,
                message = "Error message"
            )
        )
        val result = ValidationResult( issues )
        assertFalse( result.isValid )
    }

    @Test
    fun `OK companion object creates empty issues list`()
    {
        val result = ValidationResult.OK
        assertTrue( result.issues.isEmpty() )
    }

    @Test
    fun `can create ValidationResult with optional fields`()
    {
        val issue = ValidationIssue(
            severity = ValidationSeverity.ERROR,
            code = ValidationErrorCode.INVALID_UUID_FORMAT,
            message = "Error message",
            path = "/some/path",
            subjectId = "subject123"
        )
        val result = ValidationResult( listOf( issue ) )

        assertEquals( 1, result.issues.size )
        assertEquals( ValidationErrorCode.INVALID_UUID_FORMAT, result.issues[0].code )
        assertEquals( "/some/path", result.issues[0].path )
        assertEquals( "subject123", result.issues[0].subjectId )
    }

    @Test
    fun `ValidationResult data class equality`()
    {
        val issue1 = ValidationIssue(
            severity = ValidationSeverity.WARNING,
            code = ValidationErrorCode.NAMING_CONVENTION_VIOLATION,
            message = "Same warning"
        )
        val issue2 = ValidationIssue(
            severity = ValidationSeverity.WARNING,
            code = ValidationErrorCode.NAMING_CONVENTION_VIOLATION,
            message = "Same warning"
        )

        val result1 = ValidationResult( listOf( issue1 ) )
        val result2 = ValidationResult( listOf( issue2 ) )

        assertEquals( result1, result2 )
    }
}
