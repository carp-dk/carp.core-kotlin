package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import kotlin.test.*
import org.mockito.kotlin.*

class CommandLineExternalProcessTest {

    private lateinit var mockContext: ExecutionContext
    private lateinit var mockTemplate: CommandTemplate

    @BeforeTest
    fun setUp() {
        mockContext = mock()
        mockTemplate = mock()
    }

    @Test
    fun `constructor should throw if args list is empty`() {
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
    fun `getArguments should return the provided arguments`() {
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
    fun `getFormattedCommand should call render on template`() {
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
