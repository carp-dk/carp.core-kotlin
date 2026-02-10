package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertNotNull

class ExecutionStrategyTest
{
    /**
     * Example implementation showing how ExecutionStrategy implementations work
     */
    private class ExampleExecutionStrategy : ExecutionStrategy
    {
        override fun execute(workflow: Workflow, executionFactory: IExecutionFactory)
        {
            // Validate the workflow is provided
            require(workflow.metadata.name.isNotEmpty()) { "Workflow name cannot be empty" }
        }
    }

    /**
     * Mock execution factory for testing
     */
    private class MockExecutionFactory : IExecutionFactory {

        override fun <P : ExternalProcess> register(
            processType: KClass<out P>,
            executorCreator: () -> Executor
        ) {
            TODO("Not yet implemented")
        }

        override fun <P : ExternalProcess> getExecutor(process: P): Executor {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `ExecutionStrategy interface allows implementations`()
    {
        val strategy: ExecutionStrategy = ExampleExecutionStrategy()
        assertNotNull(strategy)

        val workflow = Workflow(
            metadata = WorkflowMetadata(
                "Test Workflow",
                "Test workflow description",
                UUID.randomUUID(),
            )
        )

        val factory = MockExecutionFactory()

        // Should not throw - demonstrates clean interface
        strategy.execute(workflow, factory)
    }
}
