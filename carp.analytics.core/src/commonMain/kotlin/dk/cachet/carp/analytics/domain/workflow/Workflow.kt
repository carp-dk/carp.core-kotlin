package dk.cachet.carp.analytics.domain.workflow
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a collection of steps in a workflow.
 */

@SerialName("Workflow")
@Serializable
data class Workflow(
    override val metadata: WorkflowMetadata,
) : WorkflowComponent
{
    @Contextual
    private val steps = mutableListOf<WorkflowComponent>()

    /**
     * Add a component (step or sub-workflow) to this workflow.
     */
    fun addComponent( component: WorkflowComponent )
    {
        steps.add(component)
    }

    /**
     * Add multiple components to the workflow.
     */
    fun addComponents( components: List<WorkflowComponent> )
    {
        steps.addAll(components)
    }

    /**
     * Remove a specific component from the workflow.
     */
    fun removeComponent( component: WorkflowComponent )
    {
        steps.remove(component)
    }

    /**
     * Get a copy of the ordered list of workflow components.
     */
    fun getComponents(): List<WorkflowComponent> = steps.toList()
}
