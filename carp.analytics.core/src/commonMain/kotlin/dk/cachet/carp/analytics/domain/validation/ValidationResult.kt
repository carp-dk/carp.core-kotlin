package dk.cachet.carp.analytics.domain.validation

data class ValidationResult( val issues: List<ValidationIssue> )
{
    val isValid: Boolean get() = issues.none { it.severity == ValidationSeverity.ERROR }

    companion object
    {
        val OK: ValidationResult = ValidationResult(emptyList())
    }
}
