package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.DataStatistics
import dk.cachet.carp.analytics.domain.data.ExecutionOutput
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.FileSystemSource
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.*

class ExecutionOutputTest
{

    @Test
    fun testCreateAndAccessProperties()
    {
        val location = FileSystemSource(
            path = "/data/outputs/summary.csv",
            format = FileFormat.CSV
        )
        val output = ExecutionOutput(
            outputId = "summary",
            actualLocation = location,
            statistics = DataStatistics(),
            timestamp = Clock.System.now(),
            success = true,
            errorMessage = null
        )

        assertEquals("summary", output.outputId)
        assertEquals(location, output.actualLocation)
        assertTrue(output.success)
        assertNull(output.errorMessage)
    }

    @Test
    fun testSerializationRoundTrip()
    {
        val output = ExecutionOutput(
            outputId = "heart_rate_output",
            actualLocation = FileSystemSource(
                path = "/data/outputs/heart_rate.json",
                format = FileFormat.JSON
            ),
            statistics = DataStatistics(rowCount = 100, columnCount = 3),
            timestamp = Clock.System.now(),
            success = true,
            errorMessage = null
        )

        val json = Json.encodeToString(ExecutionOutput.serializer(), output)
        val decoded = Json.decodeFromString(ExecutionOutput.serializer(), json)

        assertEquals(output.outputId, decoded.outputId)
        assertEquals(output.success, decoded.success)
    }
}
