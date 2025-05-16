package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.InputDataReference
import dk.cachet.carp.analytics.domain.data.OutputDataReference
import dk.cachet.carp.analytics.domain.process.WorkflowProcess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single step in a workflow.
 */
@SerialName("step")
@Serializable
data class Step(
    override val metadata: StepMetadata,
    val inputData: List<InputDataReference>? = null,
    val outputData: OutputDataReference? = null,
    val process: WorkflowProcess
) : WorkflowComponent
