package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.InputDataReference
import dk.cachet.carp.analytics.domain.data.OutputDataReference
import dk.cachet.carp.analytics.domain.process.Process
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single step in a workflow.
 */
@SerialName("step")
@Serializable
data class Step(
    override val name: String,
    override val description: String,
    val inputData: List<InputDataReference>,
    val outputData: OutputDataReference?,
    val process: Process
) : WorkflowComponent
