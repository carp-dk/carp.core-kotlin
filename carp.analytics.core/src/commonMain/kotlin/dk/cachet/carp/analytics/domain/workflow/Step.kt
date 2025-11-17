package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import dk.cachet.carp.analytics.domain.data.StepExecutionResult
import dk.cachet.carp.analytics.domain.data.ValidationResult
import dk.cachet.carp.analytics.domain.process.WorkflowProcess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single step in a workflow.
 * A step takes inputs, applies a process, and produces outputs.
 */
@Serializable
@SerialName( "Step" )
data class Step(
    override val metadata: StepMetadata,
    val inputs: List<InputDataSpec> = emptyList(),
    val outputs: List<OutputDataSpec> = emptyList(),
    val process: WorkflowProcess,
    val executionResult: StepExecutionResult? = null
) : WorkflowComponent {

    /**
     * Validates that this step is properly configured.
     */
    fun validate(): StepValidationResult {
        val errors = mutableListOf<String>()

        // Validate all inputs
        inputs.forEach { input ->
            val result = input.validate()
            if (result.isFailure) {
                errors.add("Input '${input.identifier}': ${(result as ValidationResult.Failure).errors.joinToString(", ")}")
            }
        }

        // Validate all outputs
        outputs.forEach { output ->
            val result = output.validate()
            if (result.isFailure) {
                errors.add("Output '${output.identifier}': ${(result as ValidationResult.Failure).errors.joinToString(", ")}")
            }
        }

        return if (errors.isEmpty()) {
            StepValidationResult.Valid
        } else {
            StepValidationResult.Invalid(errors)
        }
    }

    /**
     * Checks if this step can consume the output of another step.
     */
    fun canConsumeOutputOf(other: Step, outputId: String): Boolean {
        val otherOutput = other.outputs.find { it.identifier == outputId } ?: return false
        val matchingInput = inputs.find { it.identifier == outputId }

        if (matchingInput != null) {
            if (matchingInput.schema != null && otherOutput.schema != null) {
                return matchingInput.schema.isCompatibleWith(otherOutput.schema)
            }
            return true
        }

        return false
    }
}

/**
 * Result of step validation.
 */
@Serializable
sealed class StepValidationResult {
    @Serializable
    object Valid : StepValidationResult()

    @Serializable
    data class Invalid(val errors: List<String>) : StepValidationResult()

    val isValid: Boolean get() = this is Valid
}
