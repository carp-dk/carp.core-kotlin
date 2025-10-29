package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.ExecutionOutput
import kotlinx.serialization.json.Json
import kotlin.test.*

class ExecutionOutputTest
{

    @Test
    fun testCreateAndAccessProperties()
    {
        val location = DataLocation(
            segments = listOf("data", "outputs", "summary.csv"),
            isAbsolute = true,
            scheme = "file"
        )
        val output = ExecutionOutput("summary", "text/csv", location)

        assertEquals("summary", output.name)
        assertEquals("text/csv", output.dataType)
        assertEquals(location, output.location)
    }

    @Test
    fun testSerializationRoundTrip()
    {
        val output = ExecutionOutput(
            name = "heart_rate_output",
            dataType = "application/json",
            location = DataLocation(
                segments = listOf("data", "outputs", "summary.csv"),
                isAbsolute = true,
                scheme = "file"
            )
        )

        val json = Json.encodeToString(ExecutionOutput.serializer(), output)
        val decoded = Json.decodeFromString(ExecutionOutput.serializer(), json)

        assertEquals(output, decoded)
    }
}
