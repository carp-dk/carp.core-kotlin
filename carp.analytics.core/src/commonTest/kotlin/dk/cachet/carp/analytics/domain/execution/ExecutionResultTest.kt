package dk.cachet.carp.analytics.domain.execution


import dk.cachet.carp.analytics.domain.data.*
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Clock
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
                    outputId = "summary",
                    actualLocation = FileSystemSource(
                        path = "/data/outputs/summary.csv",
                        format = FileFormat.CSV
                    ),
                    statistics = DataStatistics(),
                    timestamp = Clock.System.now(),
                    success = true,
                    errorMessage = null
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
