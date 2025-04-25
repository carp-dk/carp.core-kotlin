package dk.cachet.carp.analytics.domain.workflow
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
/**
 * Represents a collection of steps in a workflow.
 */

@SerialName("Workflow")
@Serializable
data class Workflow(
    override val name: String,
    override val description: String
) : WorkflowComponent {
    @Contextual
    private val steps = mutableListOf<Step>()

    /**
     * Adds a step to the workflow.
     * @param step The step to add.
     */
    fun addStep(step: Step) {
        steps.add(step)
    }

    /**
     * Removes a step from the workflow.
     * @param step The step to remove.
     */
    fun removeStep(step: Step) {
        steps.remove(step)
    }

    /**
     * Returns an ordered list of steps in the workflow.
     */
    fun getSteps(): List<Step> = steps.toList()

}
