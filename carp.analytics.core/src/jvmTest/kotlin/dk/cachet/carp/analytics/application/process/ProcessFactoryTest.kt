package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.*
import org.mockito.kotlin.*
import java.nio.file.Path

class ProcessFactoryTest {

    private lateinit var context: ExecutionContext

    @BeforeTest
    fun setUp() {
        context = mock()
    }

    @Test
    fun `should create CommandLineExternalProcess`() {
        val config = mapOf(
            "commandTemplate" to "echo {}",
            "arguments" to listOf("hello"),
            "description" to "test"
        )

        val process = ProcessFactory.createProcess(
            type = ProcessType.COMMAND_LINE,
            name = "TestCmd",
            executionContext = context,
            config = config
        )

        assertTrue(process is CommandLineExternalProcess)
        assertEquals("TestCmd", process.name)
        assertEquals("test", process.description)
        assertEquals("echo {}", process.commandTemplate.template)
        assertEquals(listOf("hello"), process.args)
    }

    @Test
    fun `should create ApplicationScriptExternalProcess`() {
        val config = mapOf(
            "scriptPath" to "run.py",
            "parameters" to mapOf("input" to "data.csv"),
            "description" to "script desc"
        )

        val process = ProcessFactory.createProcess(
            type = ProcessType.APPLICATION_SCRIPT,
            name = "AppScript",
            executionContext = context,
            config = config
        )

        assertTrue(process is ApplicationScriptExternalProcess)
        assertEquals(Path.of("run.py"), process.scriptPath)
        assertEquals(mapOf("input" to "data.csv"), process.parameters)
    }

    @Test
    fun `should create PythonExternalProcess`() {
        val config = mapOf(
            "scriptPath" to ".\\scripts\\plotter.py",
            "parameters" to listOf("--input", "data.csv"),
            "description" to "py desc"
        )

        val process = ProcessFactory.createProcess(
            type = ProcessType.PYTHON_SCRIPT,
            name = "PyScript",
            executionContext = context,
            config = config
        )

        assertTrue(process is PythonExternalProcess)
        assertEquals(".\\scripts\\plotter.py", process.scriptPath)
        assertEquals(mutableListOf("--input", "data.csv"), process.args)
    }

    @Test
    fun `should create DataRetrievalProcess`() {
        val config = mapOf(
            "studyId" to "11111111-1111-1111-1111-111111111111",
            "fields" to listOf("field1", "field2"),
            "deviceRoles" to listOf("phone", "watch"),
            "from" to Clock.System.now().toString(),
            "to" to Clock.System.now().plus(24, DateTimeUnit.HOUR) .toString(),
            "offsetDays" to 2,
            "description" to "data desc"
        )

        val process = ProcessFactory.createProcess(
            type = ProcessType.DATA_RETRIEVAL,
            name = "DataFetch",
            executionContext = context,
            config = config
        )

        assertTrue(process is DataRetrievalProcess)
        assertEquals("data desc", process.description)
        assertEquals("DataFetch", process.name)
    }

    @Test
    fun `should create MeanDailyStepCountProcess`() {
        val process = ProcessFactory.createProcess(
            type = ProcessType.MEAN_DAILY_STEP_COUNT,
            name = "StepMean",
            executionContext = context,
            config = emptyMap()
        )

        assertTrue(process is MeanDailyStepCountProcess)
    }

    @Test
    fun `should use defaults when optional config is missing`() {
        val config = mapOf(
            "commandTemplate" to "echo {}",
            "arguments" to listOf("default")
        )

        val process = ProcessFactory.createProcess(
            type = ProcessType.COMMAND_LINE,
            name = "NoDesc",
            executionContext = context,
            config = config
        )

        assertNull((process as CommandLineExternalProcess).description)
    }
}
