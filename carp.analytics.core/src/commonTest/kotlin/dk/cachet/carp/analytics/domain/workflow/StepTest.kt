package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import kotlin.test.*

class FakeExternalProcess(
    override val name: String = "Fake Process",
    override val description: String? = "Fake description",
    override val executionContext: ExecutionContext = ExecutionContext(null, emptyMap()),
    val arguments: List<String> = emptyList()
) : ExternalProcess {
    override fun getArguments(): Any = arguments
}

class StepTest {

    private lateinit var step: Step

    @BeforeTest
    fun setup() {
        step = Step(
            metadata = StepMetadata(
                name = "Extract Sleep Duration",
                version = Version(1, 0)
            ),
            process = FakeExternalProcess(
                name = "Fake Sleep Process",
                description = "Simulated CLI call",
                executionContext = ExecutionContext(null, emptyMap()),
                arguments = listOf("--input", "sleep_data.csv", "--output", "result.json")
            )
        )
    }

    @Test
    fun shouldStoreMetadataCorrectly() {fun shouldStoreMetadataCorrectly() {
        assertEquals("Extract Sleep Duration", step.metadata.name)
        assertEquals(Version(1, 0), step.metadata.version)
    }
        assertEquals("Extract Sleep Duration", step.metadata.name)
        assertEquals(Version(1, 0), step.metadata.version)
    }

    @Test
    fun shouldContainFakeProcessWithCorrectValues() {
        val process = step.process as FakeExternalProcess
        assertEquals("Fake Sleep Process", process.name)
        assertEquals("Simulated CLI call", process.description)
        assertEquals(listOf("--input", "sleep_data.csv", "--output", "result.json"), process.getArguments())
    }

    @Test
    fun inputAndOutputDataShouldBeNullByDefault() {
        assertNull(step.inputData)
        assertNull(step.outputData)
    }

}
