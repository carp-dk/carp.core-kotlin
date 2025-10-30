package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertNotNull

class ExecutionStrategyTest
{
    /**
     * Example implementation showing how ExecutionStrategy implementations
     * should manage their own ExecutorFactory dependencies internally.
     */
    private class ExampleExecutionStrategy : ExecutionStrategy
    {
        // Implementation would manage its own ExecutorFactory dependency here
        // private val executorFactory: ExecutorFactory = ...

        override fun execute(workflow: Workflow)
        {
            // Implementation would use its internal executorFactory here
            // executorFactory.createExecutor().execute(workflow)

            // For this example, we just validate the workflow is provided
            require(workflow.metadata.name.isNotEmpty()) { "Workflow name cannot be empty" }
        }
    }

    @Test
    fun `ExecutionStrategy interface allows implementations to manage their own dependencies`()
    {
        val strategy: ExecutionStrategy = ExampleExecutionStrategy()
        assertNotNull(strategy)

        // Verify the interface contract - implementations only need workflow parameter
        val workflow = Workflow(
            metadata = WorkflowMetadata(
                "Test Workflow",
                "Test workflow description",
                UUID.randomUUID(),
            )
        )

        // Should not throw - demonstrates clean interface without factory dependency
        strategy.execute(workflow)
    }
}