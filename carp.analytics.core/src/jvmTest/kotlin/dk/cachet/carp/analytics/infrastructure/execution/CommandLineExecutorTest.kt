package dk.cachet.carp.analytics.infrastructure.execution


import dk.cachet.carp.analytics.domain.process.CommandLineProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.infrastructure.execution.ProcessExecutorInterface
import dk.cachet.carp.analytics.domain.execution.ExecutionContext

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

import org.mockito.kotlin.*



// class CommandLineExecutorTest {
//
//     private val mockContext = ExecutionContext(envVariables = mapOf("TEST_VAR" to "VALUE"))
//     private val commandTemplate = CommandTemplate("echo {0S} {1}")
//     private val process = CommandLineProcess(
//         name = "Test Command",
//         executionContext = mockContext,
//         commandTemplate = commandTemplate,
//         arguments = listOf("Hello", "World")
//     )
//
//     @Test
//     fun `test execute valid command`() {
//         val mockProcessExecutor = mock(ProcessExecutorInterface::class.java)
//         doNothing().`when`(mockProcessExecutor).executeCommand(anyString(), anyMap())
//
//         val executor = CommandLineExecutor(mockProcessExecutor)
//         try {
//             executor.execute(process, mockContext)
//         } catch (e: Throwable) {
//             fail("Unexpected exception: $e")
//         }
//
//         verify(mockProcessExecutor).executeCommand(process.getFormattedCommand(), mockContext.envVariables)
//     }
// }
// class TestMock {
//     interface Service { fun get(id: String): String }
//
//     @Test
//     fun resolvesAnyString() {
//         val service = mock<Service>()
//         whenever(service.get(anyString())).thenReturn("OK")
//         assertEquals("OK", service.get("abc"))
//     }
// }
