package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.validation.ValidationErrorCode
import dk.cachet.carp.analytics.domain.validation.ValidationIssue
import dk.cachet.carp.analytics.domain.validation.ValidationResult
import dk.cachet.carp.analytics.domain.validation.ValidationSeverity
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith


/**
 * Tests for [SubmitResult].
 */
class SubmitResultTest
{
    @Test
    fun can_initialize_stored_with_warnings() { SubmitResult( METADATA, ValidationResult( listOf( issue( WARNING ) ) ) ) }

    @Test
    fun can_initialize_rejected_with_errors() { SubmitResult( null, ValidationResult( listOf( issue( ERROR ) ) ) ) }

    @Test
    fun cant_initialize_stored_with_errors()
    {
        assertFailsWith<IllegalArgumentException> { SubmitResult( METADATA, ValidationResult( listOf( issue( ERROR ) ) ) ) }
    }

    @Test
    fun cant_initialize_rejected_without_errors()
    {
        assertFailsWith<IllegalArgumentException> { SubmitResult( null, ValidationResult.OK ) }
    }


    private fun issue( severity: ValidationSeverity ) =
        ValidationIssue( severity, ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE, "Duplicate step id." )


    companion object
    {
        private val ERROR = ValidationSeverity.ERROR
        private val WARNING = ValidationSeverity.WARNING
        private val METADATA = WorkflowMetadata( name = "Gait", id = UUID.randomUUID() )
    }
}
