package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.DataSpecValidationResult
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
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
            if ( !result.isValid )
            {
                errors.add(
                    "Input '${input.id}': ${( result as DataSpecValidationResult.Invalid ).errors.joinToString( ", " )}"
                )
            }
        }

        // Validate all outputs
        outputs.forEach { output ->
            val result = output.validate()
            if ( !result.isValid )
            {
                errors.add(
                    "Output '${output.id}': ${(
                            result as DataSpecValidationResult.Invalid
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
