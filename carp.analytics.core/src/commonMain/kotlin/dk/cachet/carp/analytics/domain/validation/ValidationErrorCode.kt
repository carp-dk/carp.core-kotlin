package dk.cachet.carp.analytics.domain.validation

enum class ValidationErrorCode( val value: String )
{
    // Workflow-level errors
    WORKFLOW_STEP_ID_DUPLICATE("workflow-step-id-duplicate"),
    WORKFLOW_MISSING_ENVIRONMENT("workflow-missing-environment"),
    WORKFLOW_UNKNOWN_ENV_KIND("workflow-unknown-env-kind"),
    WORKFLOW_DEP_REFERENCE_MISSING("workflow-dep-reference-missing"),
    WORKFLOW_DEP_CYCLE_DETECTED("workflow-dep-cycle-detected"),

    // Step-level errors
    STEP_INPUT_PORT_DUPLICATE_ID("step-input-port-duplicate-id"),
    STEP_OUTPUT_PORT_DUPLICATE_ID("step-output-port-duplicate-id"),
    STEP_INPUT_REQUIRED_MISSING("step-input-required-missing"),
    STEP_SPEC_TYPE_MISSING("step-spec-type-missing"),

    // Naming & style warnings
    NAMING_CONVENTION_VIOLATION("naming-convention-violation"),
    DEPRECATED_VOCABULARY_DETECTED("deprecated-vocabulary-detected"),

    // Metadata warnings
    MISSING_METADATA("missing-metadata"),

    // Resource warnings
    UNUSED_ENVIRONMENT("unused-environment"),
    WORKFLOW_TOO_LONG("workflow-too-long"),

    // Argument errors
    ARGUMENT_INVALID_INPUT_REF("argument-invalid-input-ref"),
    ARGUMENT_INVALID_OUTPUT_REF("argument-invalid-output-ref"),

    // Format errors
    INVALID_UUID_FORMAT("invalid-uuid-format");

    val category: String
        get() = when {
            name.startsWith("WORKFLOW_") -> "workflow"
            name.startsWith("STEP_") -> "step"
            name.startsWith("NAMING_") -> "naming"
            name.startsWith("DEPRECATED_") -> "deprecation"
            name.startsWith("UNUSED_") -> "resources"
            name.startsWith("ARGUMENT_") -> "arguments"
            else -> "general"
        }
}
