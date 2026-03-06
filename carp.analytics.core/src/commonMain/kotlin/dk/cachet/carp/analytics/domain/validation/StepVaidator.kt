package dk.cachet.carp.analytics.domain.validation

/**
 * Author-time validation for a "Step-like" object.
 *
 * This validator stays reusable by accepting extractors (lambdas) rather than depending on a specific Step model shape.
 * It provides a flexible configuration that allows validation of steps with different structures.
 *
 * Usage:
 * ```
 * val config = StepValidator.Config(
 *     stepId = { step -> step.id },
 *     outputIds = { step -> step.outputs.map { it.id } },
 *     missingRequiredInputSpecs = { step -> findMissingRequired(step) },
 *     specsMissingTypes = { step -> findMissingTypes(step) }
 * )
 * val result = StepValidator.validate(step, config)
 * ```
 */
object StepValidator
{

    /**
     * Configuration for validating a step-like object.
     *
     * @param S The step type to validate
     * @param stepId Extract the unique id from a step
     * @param outputIds Extract all output IDs from a step (used to check uniqueness)
     * @param missingRequiredInputSpecs Extract missing required input specifications (defaults to empty list)
     * @param specsMissingTypes Extract specification identifiers that are missing type information (defaults to empty list)
     * @param stepPath Generate a path string for error reporting (defaults to "steps[{stepId}]")
     */
    data class Config<S>(
        val stepId: (S) -> String,
        val outputIds: (S) -> List<String>,
        val missingRequiredInputSpecs: (S) -> List<String> = { emptyList() },
        val specsMissingTypes: (S) -> List<String> = { emptyList() },
        val stepPath: (S) -> String = { s -> "steps[${stepId(s)}]" }
    )

    /**
     * Validates a step against the provided configuration.
     *
     * Performs the following checks:
     * - Uniqueness of output IDs within the step
     * - Presence of required input specifications
     * - Presence of type information for specifications
     *
     * @param step The step instance to validate
     * @param config The validation configuration with extractors
     * @return A [ValidationResult] containing any validation issues found, or [ValidationResult.OK] if all checks pass
     */
    fun <S> validate( step: S, config: Config<S> ): ValidationResult
    {
        val issues = buildList {
            addAll(checkUniqueOutputIds(step, config))
            addAll(checkRequiredInputs(step, config))
            addAll(checkSpecTypesPresent(step, config))
        }
        return if (issues.isEmpty()) ValidationResult.OK else ValidationResult(issues)
    }

    /**
     * Checks that all output IDs within a step are unique.
     *
     * @param step The step to validate
     * @param config The validation configuration
     * @return A list of validation issues for each duplicate output ID found
     */
    private fun <S> checkUniqueOutputIds( step: S, config: Config<S> ): List<ValidationIssue>
    {
        val outputs = config.outputIds(step)
        if (outputs.isEmpty()) return emptyList()

        val duplicates = outputs
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        if (duplicates.isEmpty()) return emptyList()

        val sid = config.stepId(step)
        val basePath = config.stepPath(step)

        return duplicates.map { dup ->
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.STEP_OUTPUT_PORT_DUPLICATE_ID,
                message = "Step '$sid' has duplicate output id '$dup'. Output ids must be unique within a step.",
                path = "$basePath.outputs",
                subjectId = sid
            )
        }
    }

    /**
     * Checks that all required input specifications are present.
     *
     * @param step The step to validate
     * @param config The validation configuration
     * @return A list of validation issues for each missing required input specification
     */
    private fun <S> checkRequiredInputs( step: S, config: Config<S> ): List<ValidationIssue>
    {
        val missing = config.missingRequiredInputSpecs(step)
        if (missing.isEmpty()) return emptyList()

        val sid = config.stepId(step)
        val basePath = config.stepPath(step)

        return missing.map { m ->
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.STEP_INPUT_REQUIRED_MISSING,
                message = "Step '$sid' is missing required input spec '$m'.",
                path = "$basePath.inputs",
                subjectId = sid
            )
        }
    }

    /**
     * Checks that all specifications have required type information.
     *
     * @param step The step to validate
     * @param config The validation configuration
     * @return A list of validation issues for each specification missing type information
     */
    private fun <S> checkSpecTypesPresent( step: S, config: Config<S> ): List<ValidationIssue>
    {
        val missingTypes = config.specsMissingTypes(step)
        if (missingTypes.isEmpty()) return emptyList()

        val sid = config.stepId(step)
        val basePath = config.stepPath(step)

        return missingTypes.map { spec ->
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.STEP_SPEC_TYPE_MISSING,
                message = "Step '$sid' has spec '$spec' with missing type, but types are required by the model.",
                path = "$basePath.specs",
                subjectId = sid
            )
        }
    }
}

/**
 * Internal enum used for cycle detection in the DFS algorithm.
 * - TEMP: Node is currently being processed (on the call stack)
 * - PERM: Node has been fully processed
 */
enum class Mark { TEMP, PERM }
