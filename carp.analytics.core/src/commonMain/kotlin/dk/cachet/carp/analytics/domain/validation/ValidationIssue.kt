package dk.cachet.carp.analytics.domain.validation

data class ValidationIssue(
    val severity: ValidationSeverity,
    val code: String,
    val message: String,
    val path: String? = null,
    val subjectId: String? = null
)
