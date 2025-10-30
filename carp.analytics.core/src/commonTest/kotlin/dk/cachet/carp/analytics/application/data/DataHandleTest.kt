package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Geolocation
import dk.cachet.carp.data.application.MutableDataStreamBatch
import dk.cachet.carp.data.application.MutableDataStreamSequence
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.data.infrastructure.dataStreamId
import dk.cachet.carp.data.infrastructure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals

class DataHandleTest
{

    @Test
    fun `in memory data holds dataset`()
    {
        val deploymentId = UUID( "c9cc5317-48da-45f2-958e-58bc07f34681" )
        val phoneGeoDataStream = dataStreamId<Geolocation>( deploymentId, "Participant's phone" )

        // Create a measurement with Geolocation data
        val measurementData = measurement(
            data = Geolocation(55.680619, 12.582050),
            sensorStartTime = 1642505045000000, // microseconds
            sensorEndTime = null // point measurement
        )

        // Create a data stream sequence
        val sequence = MutableDataStreamSequence<Geolocation>(
            dataStream = phoneGeoDataStream,
            firstSequenceId = 0,
            triggerIds = listOf(1),
            syncPoint = SyncPoint.UnixEpoch
        )
        sequence.appendMeasurements(measurementData)

        // Create a batch containing the sequence
        val mutableBatch = MutableDataStreamBatch()
        mutableBatch.appendSequence(sequence)


        val handle = InMemoryData(mutableBatch)
    }

    @Test
    fun `file data holds path and mimetype`()
    {
        val file = FileData("/tmp/test.csv", "text/csv")
        assertEquals("/tmp/test.csv", file.path)
        assertEquals("text/csv", file.mimeType)
    }
}
