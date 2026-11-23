package dk.cachet.carp.analytics.application

import dk.cachet.carp.analytics.domain.validation.ValidationResult
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import kotlinx.serialization.Serializable


/**
 * The outcome of submitting a workflow.
 *
 * @property workflow The stored workflow, or null when [validation] has errors and nothing was stored.
 * @property validation All issues found, including warnings on a stored workflow.
 */
@Serializable
data class SubmitResult(
    val workflow: WorkflowMetadata?,
    val validation: ValidationResult
)
{
    init
    {
        require( (workflow != null) == validation.isValid )
            { "A workflow is stored if and only if it has no errors." }
    }
}
