package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.test.assertFailsWith

class CommandLineExecutorTest {

    private val mockContext = ExecutionContext(envVariables = mapOf("TEST_VAR" to "VALUE"))
    private val commandTemplate = CommandTemplate("echo {0S} {1}")
    private val process = CommandLineExternalProcess(
        name = "Test Command",
        executionContext = mockContext,
        commandTemplate = commandTemplate,
        args = listOf("Hello", "World"),
        description = "A test command"
    )

    @Test
    fun `execute runs expected command without error`() {
        // Arrange
        val mockProcessExecutor = mock<ProcessExecutorInterface>()
        val executor = CommandLineExecutor(mockProcessExecutor)

        whenever(mockProcessExecutor.executeCommand(any(), any(),anyOrNull())).then { }

        // Act
        executor.execute(process, mockContext)

        // Assert
        verify(mockProcessExecutor).executeCommand(
            eq(process.getFormattedCommand()),
            eq(mockContext.envVariables),
            anyOrNull()
        )
    }

    @Test
    fun `execute throws error if process fails`() {
        // Arrange
        val mockProcessExecutor = mock<ProcessExecutorInterface>()
        val executor = CommandLineExecutor(mockProcessExecutor)

        whenever(mockProcessExecutor.executeCommand(
            eq(process.getFormattedCommand()),
            eq(mockContext.envVariables),
            anyOrNull()
        )).thenThrow(RuntimeException("Execution failed"))

        // Assert + Act
        assertFailsWith<RuntimeException>("Expected exception when execution fails") {
            executor.execute(process, mockContext)
        }

        verify(mockProcessExecutor).executeCommand(
            eq(process.getFormattedCommand()),
            eq(mockContext.envVariables),
            anyOrNull()
        )
    }

}
