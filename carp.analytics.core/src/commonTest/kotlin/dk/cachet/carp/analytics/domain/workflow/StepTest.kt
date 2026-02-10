package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

class FakeExternalProcess(
    override val name: String = "Fake Process",
    override val description: String? = "Fake description",
    val arguments: List<String> = emptyList()
) : ExternalProcess
{
    override fun getArguments(): Any = arguments
}

class StepTest
{

    private lateinit var step: Step

    @BeforeTest
    fun setup()
    {
        step = Step(
            metadata = StepMetadata(
                id = UUID.randomUUID(),
                name = "Extract Sleep Duration",
                version = Version(1, 0)
            ),
            process = FakeExternalProcess(
                name = "Fake Sleep Process",
                description = "Simulated CLI call",
                arguments = listOf("--input", "sleep_data.csv", "--output", "result.json")
            )
        )
    }

    @Test
    fun shouldStoreMetadataCorrectly()
    {
        assertEquals("Extract Sleep Duration", step.metadata.name)
        assertEquals(Version(1, 0), step.metadata.version)
    }

    @Test
    fun shouldContainFakeProcessWithCorrectValues()
    {
        val process = step.process as FakeExternalProcess
        assertEquals("Fake Sleep Process", process.name)
        assertEquals("Simulated CLI call", process.description)
        assertEquals(listOf("--input", "sleep_data.csv", "--output", "result.json"), process.getArguments())
    }

    @Test
    fun inputAndOutputDataShouldBeEmptyByDefault()
    {
        assertTrue(step.inputs.isEmpty())
        assertTrue(step.outputs.isEmpty())
    }
}
