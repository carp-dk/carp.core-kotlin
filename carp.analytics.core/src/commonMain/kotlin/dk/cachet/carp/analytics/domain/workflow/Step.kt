package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.InputData
import dk.cachet.carp.analytics.domain.data.OutputData
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
    val inputData: List<InputData>,
    val outputData: OutputData?, 
    val process: Process
) : WorkflowComponent
