package dk.cachet.carp.analytics.application.execution


import dk.cachet.carp.analytics.application.runtime.RuntimeDependencyKey
import dk.cachet.carp.analytics.domain.process.InjectableProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowComponent

/**
 * Utility for injecting runtime dependencies into [Workflow] components.
 *
 * This is primarily used to wire up dependencies like services or configuration
 * objects into processes (e.g., data access layers) that require them at runtime.
 */
object WorkflowInjector {

    /**
     * Inject dependencies into all [InjectableProcess] instances within the given [workflow].
     *
     * This operation is recursive, supporting workflows that include sub-workflows.
     *
     * @param workflow The root workflow to inject dependencies into.
     * @param dependencies A map of [RuntimeDependencyKey]s to their corresponding instances.
     */
    fun inject(
        workflow: Workflow,
        dependencies: Map<RuntimeDependencyKey<*>, Any>
    ) {
        workflow.getComponents().forEach { component ->
            injectComponent(component, dependencies)
        }
    }

    private fun injectComponent(
        component: WorkflowComponent,
        dependencies: Map<RuntimeDependencyKey<*>, Any>
    ) {
        when (component) {
            is Step -> {
                val process = component.process
                if (process is InjectableProcess) {
                    process.inject(dependencies)
                }
            }
            is Workflow -> {
                inject(component, dependencies)
            }
        }
    }
}