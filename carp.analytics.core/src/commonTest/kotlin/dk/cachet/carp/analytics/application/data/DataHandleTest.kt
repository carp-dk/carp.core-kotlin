package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Geolocation
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.infrastructure.dataStreamId
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DataHandleTest
{

    @Test
    fun `in memory data holds dataset`()
    {
        val deploymentId = UUID( "c9cc5317-48da-45f2-958e-58bc07f34681" )
        val phoneGeoDataStream = dataStreamId<Geolocation>( deploymentId, "Participant's phone" )
        val dataset = CollectedDataSet(
            points = listOf(
                CollectedDataPoint(
                    streamId = phoneGeoDataStream,
                    timestamp = Instant.fromEpochMilliseconds(1642505045000),
                    data = Geolocation(55.680619, 12.582050)
                )
            )
        )
        val handle = InMemoryData(dataset)
        assertEquals(dataset, handle.dataset)
    }

    @Test
    fun `file data holds path and mimetype`()
    {
        val file = FileData("/tmp/test.csv", "text/csv")
        assertEquals("/tmp/test.csv", file.path)
        assertEquals("text/csv", file.mimeType)
    }
}
