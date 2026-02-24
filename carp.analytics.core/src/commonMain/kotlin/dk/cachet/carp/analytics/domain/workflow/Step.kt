package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import dk.cachet.carp.analytics.domain.data.ValidationResult
import dk.cachet.carp.analytics.domain.tasks.TaskDefinition
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Declarative model of a workflow step.
 *
 * Contains definition data (task, inputs, outputs, environment reference).
 */
@Serializable
@SerialName( "Step" )
data class Step(
    override val metadata: StepMetadata,
    val inputs: List<InputDataSpec> = emptyList(),
    val outputs: List<OutputDataSpec> = emptyList(),
    val task: TaskDefinition,
    val environmentId: UUID
) : WorkflowComponent
{

    /**
     * Validates that this step is properly configured.
     */
    fun validate(): StepValidationResult
    {
        val errors = mutableListOf<String>()

        // Validate all inputs
        inputs.forEach { input ->
            val result = input.validate()
            if ( result.isFailure )
            {
                errors.add(
                    "Input '${input.id}': ${( result as ValidationResult.Failure ).errors.joinToString( ", " )}"
                )
            }
        }

        // Validate all outputs
        outputs.forEach { output ->
            val result = output.validate()
            if ( result.isFailure )
            {
                errors.add(
                    "Output '${output.id}': ${(
                            result as ValidationResult.Failure
                            ).errors.joinToString( ", " )}"
                )
            }
        }

        return if ( errors.isEmpty() )
        {
            StepValidationResult.Valid
        }
        else
        {
            StepValidationResult.Invalid( errors )
        }
    }

    /**
     * Checks if this step can consume the output of another step.
     */
    fun canConsumeOutputOf( other: Step, outputId: UUID ): Boolean
    {
        val otherOutput = other.outputs.find { it.id == outputId } ?: return false
        val matchingInput = inputs.find { it.id == outputId }

        if ( matchingInput != null )
        {
            if ( matchingInput.schema != null && otherOutput.schema != null )
            {
                return matchingInput.schema.isCompatibleWith( otherOutput.schema )
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
sealed class StepValidationResult
{
    @Serializable
    object Valid : StepValidationResult()

    @Serializable
    data class Invalid( val errors: List<String> ) : StepValidationResult()

    val isValid: Boolean
        get() = this is Valid
}
