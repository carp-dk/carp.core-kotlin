package dk.cachet.carp.analytics.infrastructure.execution


import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.domain.execution.ExecutionContext

import kotlin.test.Test
import kotlin.test.fail

import org.mockito.Mockito.mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.anyMap
import org.mockito.kotlin.*


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
    fun testExecuteValidCommand() {
        val mockProcessExecutor = mock(ProcessExecutorInterface::class.java)
        doNothing().`when`(mockProcessExecutor).executeCommand(anyString(), anyMap())

        val executor = CommandLineExecutor(mockProcessExecutor)
        try {
            executor.execute(process, mockContext)
        } catch (e: Throwable) {
            fail("Unexpected exception: $e")
        }

        verify(mockProcessExecutor).executeCommand(process.getFormattedCommand(), mockContext.envVariables)
    }
}
