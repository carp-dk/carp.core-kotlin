package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.execution.ExecutionStrategy
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.test.*
import org.mockito.kotlin.*

class ExecutionEngineTest {

    private lateinit var engine: ExecutionEngine
    private lateinit var strategy: ExecutionStrategy
    private lateinit var workflow: Workflow
    private lateinit var factory: ExecutorFactory

    @BeforeTest
    fun setUp() {
        engine = ExecutionEngine()
        strategy = mock()
        factory = mock()

        workflow = mock {
            on { metadata } doReturn WorkflowMetadata("MockWorkflow", "Mock description", UUID("00000000-0000-0000-0000-000000000123"), Version(1, 0))
        }
    }

    @Test
    fun `executeWorkflow should delegate to strategy`() {
        engine.executeWorkflow(workflow, strategy, factory)

        verify(strategy).execute(workflow, factory)
    }

    @Test
    fun `executeWorkflow should throw exception if strategy fails`() {
        whenever(strategy.execute(any(), any())).thenThrow(RuntimeException("Test failure"))

        val ex = assertFailsWith<RuntimeException> {
            engine.executeWorkflow(workflow, strategy, factory)
        }

        assertEquals("Test failure", ex.message)
        verify(strategy).execute(workflow, factory)
    }
}
