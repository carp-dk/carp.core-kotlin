package dk.cachet.carp.analytics.application.authoring

import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID

/**
 * Mutable builder for assembling an immutable [WorkflowDefinition].
 *
 * This lives in the application/authoring layer to keep the domain model immutable and pure.
 */
class WorkflowDefinitionBuilder(
    private val workflowMetadata: WorkflowMetadata
)
{
    private val components: MutableList<WorkflowComponent> = mutableListOf()
    private val environments: MutableList<EnvironmentDefinition> = mutableListOf()

    fun addComponent( component: WorkflowComponent ): WorkflowDefinitionBuilder = apply {
        components += component
    }

    fun addEnvironment( environment: EnvironmentDefinition ): WorkflowDefinitionBuilder = apply {
        environments += environment
    }

    fun addEnvironments( vararg envs: EnvironmentDefinition ): WorkflowDefinitionBuilder = apply {
        environments += envs
    }

    /**
     * Build the immutable [WorkflowDefinition].
     *
     * Note: This builder does not validate invariants; call your validators separately.
     */
    fun build(): WorkflowDefinition
    {
        val workflow = Workflow(metadata = workflowMetadata)
        components.forEach { workflow.addComponent(it) }

        return WorkflowDefinition(
            workflow = workflow,
            environments = environments.associateBy { it.id }
        )
    }

    /**
     * Convenience helper: find an environment by id if already added.
     */
    fun hasEnvironment( id: UUID ): Boolean = environments.any { it.id == id }
}
