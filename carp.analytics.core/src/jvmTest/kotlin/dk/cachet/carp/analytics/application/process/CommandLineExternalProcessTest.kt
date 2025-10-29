package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandLineExternalProcessTest
{

    private lateinit var mockContext: ExecutionContext
    private lateinit var mockTemplate: CommandTemplate

    @BeforeTest
    fun setUp()
    {
        mockContext = mock()
        mockTemplate = mock()
    }

    @Test
    fun `constructor should throw if args list is empty`()
    {
        val exception = assertFailsWith<IllegalArgumentException> {
            CommandLineExternalProcess(
                name = "TestProcess",
                description = "desc",
                executionContext = mockContext,
                commandTemplate = mockTemplate,
                args = emptyList()
            )
        }

        assertEquals("Command arguments cannot be empty", exception.message)
    }

    @Test
    fun `getArguments should return the provided arguments`()
    {
        val args = listOf("arg1", "arg2")
        val process = CommandLineExternalProcess(
            name = "TestProcess",
            description = null,
            executionContext = mockContext,
            commandTemplate = mockTemplate,
            args = args
        )

        assertEquals(args, process.getArguments())
    }

    @Test
    fun `getFormattedCommand should call render on template`()
    {
        val args = listOf("input.txt", "output.csv")
        whenever(mockTemplate.render(args)).thenReturn("echo input.txt > output.csv")

        val process = CommandLineExternalProcess(
            name = "FormatTest",
            description = null,
            executionContext = mockContext,
            commandTemplate = mockTemplate,
            args = args
        )

        val result = process.getFormattedCommand()

        assertEquals("echo input.txt > output.csv", result)
        verify(mockTemplate).render(args)
    }
}
