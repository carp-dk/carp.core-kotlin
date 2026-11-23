package dk.cachet.carp.analytics.domain.validation

import kotlinx.serialization.Serializable


/**
 * A problem found when checking a workflow.
 *
 * @property path Where in the workflow the problem is, e.g., a step id, when known.
 * @property subjectId The id of the step, input or output the issue is about, when known.
 */
@Serializable
data class ValidationIssue(
    val severity: ValidationSeverity,
    val code: ValidationErrorCode,
    val message: String,
    val path: String? = null,
    val subjectId: String? = null
)
