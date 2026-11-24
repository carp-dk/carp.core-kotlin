package dk.cachet.carp.analytics.domain.validation

import kotlinx.serialization.Serializable


/**
 * Collection of issues found when checking a workflow.
 */
@Serializable
data class ValidationResult( val issues: List<ValidationIssue> )
{
    /**
     * True when no issue has [ValidationSeverity.ERROR].
     */
    val isValid: Boolean get() = issues.none { it.severity == ValidationSeverity.ERROR }

    companion object
    {
        val OK: ValidationResult = ValidationResult( emptyList() )
    }
}
