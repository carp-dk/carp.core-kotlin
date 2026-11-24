package dk.cachet.carp.analytics.domain.validation


/**
 * Codes for the structural problems the core validators report.
 * Use [name] as [ValidationIssue.code]. Runtimes can report further codes of their own.
 */
enum class ValidationErrorCode
{
    // Workflow
    WORKFLOW_STEP_ID_DUPLICATE,
    WORKFLOW_MISSING_ENVIRONMENT,
    WORKFLOW_DEP_REFERENCE_MISSING,
    WORKFLOW_DEP_CYCLE_DETECTED,

    // Step
    STEP_INPUT_PORT_DUPLICATE_ID,
    STEP_OUTPUT_PORT_DUPLICATE_ID,
    STEP_INPUT_REQUIRED_MISSING,
    STEP_SPEC_TYPE_MISSING
}
