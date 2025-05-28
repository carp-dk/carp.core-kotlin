package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencies
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencyKey
import dk.cachet.carp.analytics.domain.process.InjectableProcess
import dk.cachet.carp.analytics.domain.process.WorkflowProcess
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class WorkflowInjectorTest {

    class TestInjectableProcess : InjectableProcess, WorkflowProcess {
        var injected: Boolean = false
        override val name: String = "TestInjectableProcess"
        override val description: String = "A test process for injection."

        override fun inject(dependencies: Map<RuntimeDependencyKey<*>, Any>) {
            if (dependencies.containsKey(RuntimeDependencies.DataRegistry)) {
                injected = true
            }
        }
    }

    class TestProcess : WorkflowProcess {
        override val name: String = "TestInjectableProcess"
        override val description: String = "A test process for injection."
    }

    @Test
    fun `inject should call inject on injectable process`() {
        val process = TestInjectableProcess()
        val step = Step(StepMetadata(name = "test"), process = process)
        val workflow = Workflow(WorkflowMetadata(name = "test", id = UUID.randomUUID(),)).apply { addComponent(step) }

        val dependencies: Map<RuntimeDependencyKey<*>, Any> =
            mapOf(RuntimeDependencies.DataRegistry as RuntimeDependencyKey<*> to DataRegistry())

        WorkflowInjector.inject(workflow, dependencies)

        assertTrue(process.injected, "The process should have had dependencies injected.")
    }

    @Test
    fun `inject should recurse into nested workflows`() {
        val process = TestInjectableProcess()
        val nestedStep = Step(StepMetadata(name = "nested test"), process = process)
        val nestedWorkflow =  Workflow(WorkflowMetadata(name = "nested workflow", id = UUID.randomUUID(),)).apply { addComponent(nestedStep) }
        val mainWorkflow =  Workflow(WorkflowMetadata(name = "test workflow", id = UUID.randomUUID(),)).apply { addComponent(nestedWorkflow) }

        val dependencies: Map<RuntimeDependencyKey<*>, Any> =
            mapOf(RuntimeDependencies.DataRegistry as RuntimeDependencyKey<*> to DataRegistry())

        WorkflowInjector.inject(mainWorkflow, dependencies)

        assertTrue(process.injected, "Injection should propagate to nested workflows.")
    }

    @Test
    fun `inject should skip non-injectable processes`() {
        val process = TestProcess() // not injectable
        val step = Step(StepMetadata(name = "non inject test"), process = process)
        val workflow = Workflow(WorkflowMetadata(name = "test workflow", id = UUID.randomUUID(),)).apply { addComponent(step) }

        val dependencies: Map<RuntimeDependencyKey<*>, Any> =
            mapOf(RuntimeDependencies.DataRegistry as RuntimeDependencyKey<*> to DataRegistry())

        // Should not throw
        WorkflowInjector.inject(workflow, dependencies)
    }
}
