package dk.cachet.carp.analytics.domain.execution


import dk.cachet.carp.analytics.domain.data.*
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.json.Json
import kotlin.test.*

class ExecutionResultTest
{

    @Test
    fun testBasicExecutionResultSerialization()
    {
        val result = BasicExecutionResult(
            executionId = UUID.randomUUID(),
            status = ExecutionStatus.COMPLETED,
            outputs = listOf(
                ExecutionOutput(
                    "summary",
                    "csv",
                    DataLocation(
                                segments = listOf("data", "outputs", "summary.csv"),
                                isAbsolute = true,
                                scheme = "file"
                    )
                )
            ),
            artifacts = listOf(
                ExecutionArtifact("/artifacts/plot.png", "Plot", ArtifactType.IMAGE, "image/png")
            )
        )

        val json = Json.encodeToString(BasicExecutionResult.serializer(), result)
        val decoded = Json.decodeFromString(BasicExecutionResult.serializer(), json)

        assertEquals(result, decoded)
    }

    @Test
    fun testEmptyOutputsAndArtifacts()
    {
        val result = BasicExecutionResult(
            executionId = UUID.randomUUID(),
            status = ExecutionStatus.FAILURE,
            outputs = null,
            artifacts = emptyList()
        )

        assertNull(result.outputs)
        assertTrue(result.artifacts.isEmpty())
    }
}
