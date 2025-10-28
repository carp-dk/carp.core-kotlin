package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.data.application.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*


/**
 * Tests for [InMemoryDataStreamService.getBatchForStudyDeployments] implementation.
 */
class InMemoryDataStreamServiceBatchRetrievalTest
{
    private lateinit var service: InMemoryDataStreamService
    private lateinit var deploymentId1: UUID
    private lateinit var deploymentId2: UUID
    private lateinit var deploymentId3: UUID
    private lateinit var dataType1: DataType
    private lateinit var dataType2: DataType

    @BeforeTest
    fun setUp()
    {
        service = InMemoryDataStreamService()
        deploymentId1 = UUID.randomUUID()
        deploymentId2 = UUID.randomUUID()
        deploymentId3 = UUID.randomUUID()
        dataType1 = DataType("test", "type1")
        dataType2 = DataType("test", "type2")
    }

    @Test
    fun getBatchForStudyDeployments_succeeds_with_mixed_valid_invalid_deployment_ids() = runTest {
        // Configure only one deployment
        val dataStreamId = DataStreamId(deploymentId1, "device1", dataType1)
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId)
        val configuration = DataStreamsConfiguration(deploymentId1, setOf(expectedStream))
        service.openDataStreams(configuration)

        // Query with mix of configured and unconfigured deployment IDs - should succeed
        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1, deploymentId2),
            deviceRoleNames = null,
            dataTypes = null,
            from = null,
            to = null
        )

        // Should succeed and return empty result (no data added yet)
        assertNotNull(result)
        assertTrue(result.sequences.toList().isEmpty())
    }

    @Test
    fun getBatchForStudyDeployments_returns_empty_result_for_no_data() = runTest {
        // Configure deployment but don't add any data
        val dataStreamId = DataStreamId(deploymentId1, "device1", dataType1)
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId)
        val configuration = DataStreamsConfiguration(deploymentId1, setOf(expectedStream))
        service.openDataStreams(configuration)

        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = null,
            to = null
        )

        assertTrue(result.sequences.toList().isEmpty())
    }

    @Test
    fun getBatchForStudyDeployments_aggregates_matching_streams() = runTest {
        // Set up multiple deployments with different data streams
        val dataStreamId1 = DataStreamId(deploymentId1, "device1", dataType1)
        val dataStreamId2 = DataStreamId(deploymentId1, "device2", dataType2)
        val dataStreamId3 = DataStreamId(deploymentId2, "device1", dataType1)

        val config1 = DataStreamsConfiguration(
            deploymentId1,
            setOf(
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId1),
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId2)
            )
        )
        val config2 = DataStreamsConfiguration(
            deploymentId2,
            setOf(DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId3))
        )

        service.openDataStreams(config1)
        service.openDataStreams(config2)

        // Add data to both deployments
        val batch1 = createTestBatch(deploymentId1, "device1", dataType1, 1000L, 3)
        val batch2 = createTestBatch(deploymentId1, "device2", dataType2, 2000L, 2)
        val batch3 = createTestBatch(deploymentId2, "device1", dataType1, 3000L, 2)

        service.appendToDataStreams(deploymentId1, batch1)
        service.appendToDataStreams(deploymentId1, batch2)
        service.appendToDataStreams(deploymentId2, batch3)

        // Query for both deployments
        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1, deploymentId2),
            deviceRoleNames = null,
            dataTypes = null,
            from = null,
            to = null
        )

        // Should get all 3 sequences (2 from deployment1, 1 from deployment2)
        // Since they are mapped by deployment and dataType
        assertEquals(3, result.sequences.toList().size)

        // Verify we get data from both deployments
        val deployment1Points = result.sequences.filter { it.dataStream.studyDeploymentId == deploymentId1 }
        assertEquals(2, deployment1Points.toList().size)

        val deployment2Points = result.sequences.filter { it.dataStream.studyDeploymentId == deploymentId2 }
        assertEquals(1, deployment2Points.toList().size)

        // Verify ordering and non-overlap per stream
        assertPerStreamOrderAndNonOverlap(result)
    }

    @Test
    fun getBatchForStudyDeployments_filters_by_device_role_names() = runTest {
        val dataStreamId1 = DataStreamId(deploymentId1, "device1", dataType1)
        val dataStreamId2 = DataStreamId(deploymentId1, "device2", dataType1)

        val config = DataStreamsConfiguration(
            deploymentId1,
            setOf(
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId1),
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId2)
            )
        )
        service.openDataStreams(config)

        // Add data to both devices
        val batch1 = createTestBatch(deploymentId1, "device1", dataType1, 1000L, 3)
        val batch2 = createTestBatch(deploymentId1, "device2", dataType1, 2000L, 2)

        service.appendToDataStreams(deploymentId1, batch1)
        service.appendToDataStreams(deploymentId1, batch2)

        // Query only for device1
        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = setOf("device1"),
            dataTypes = null,
            from = null,
            to = null
        )

        // Should only get data from device1
        assertEquals(1, result.sequences.toList().size)
        assertTrue(result.sequences.toList().all { it.dataStream.deviceRoleName == "device1" })

        // Verify ordering and non-overlap per stream
        assertPerStreamOrderAndNonOverlap(result)
    }

    @Test
    fun getBatchForStudyDeployments_filters_by_data_types() = runTest {
        val dataStreamId1 = DataStreamId(deploymentId1, "device1", dataType1)
        val dataStreamId2 = DataStreamId(deploymentId1, "device1", dataType2)

        val config = DataStreamsConfiguration(
            deploymentId1,
            setOf(
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId1),
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId2)
            )
        )
        service.openDataStreams(config)

        // Add data with different data types
        val batch1 = createTestBatch(deploymentId1, "device1", dataType1, 1000L, 3)
        val batch2 = createTestBatch(deploymentId1, "device1", dataType2, 2000L, 2)

        service.appendToDataStreams(deploymentId1, batch1)
        service.appendToDataStreams(deploymentId1, batch2)

        // Query only for dataType1
        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = setOf(dataType1),
            from = null,
            to = null
        )


        // Should only get data with dataType1
        assertEquals(1, result.sequences.toList().size)
        assertTrue(result.sequences.all { it.dataStream.dataType == dataType1 })

        // Verify ordering and non-overlap per stream
        assertPerStreamOrderAndNonOverlap(result)
    }

    @Test
    fun getBatchForStudyDeployments_filters_by_time_range() = runTest {
        val dataStreamId = DataStreamId(deploymentId1, "device1", dataType1)
        val expectedStream = DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId)
        val config = DataStreamsConfiguration(deploymentId1, setOf(expectedStream))
        service.openDataStreams(config)

        // Add data with different timestamps (in microseconds since epoch)
        val batch = createTestBatch(deploymentId1, "device1", dataType1, 1000000L, 5)
        service.appendToDataStreams(deploymentId1, batch)

        // Query with time range (converting microseconds to milliseconds for Instant)
        val fromTime = Instant.fromEpochMilliseconds(1002L) // Should include points from 1002000 microseconds onwards
        val toTime = Instant.fromEpochMilliseconds(1004L) // Should include points up to 1004000 microseconds

        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = fromTime,
            to = toTime
        )

        // Should get points with timestamps in the range
        assertTrue(result.sequences.toList().isNotEmpty())
        // Check that all measurements fall within the expected time range by iterating through sequences
        result.sequences.forEach { sequence ->
            sequence.measurements.forEach { measurement ->
                val absoluteStartTime = Instant.fromEpochMilliseconds(
                    sequence.syncPoint.applyToTimestamp(measurement.sensorStartTime) / 1000L
                )
                assertTrue(absoluteStartTime in fromTime..<toTime)
            }
        }

        // Verify ordering and non-overlap per stream
        assertPerStreamOrderAndNonOverlap(result)
    }

    @Test
    fun getBatchForStudyDeployments_returns_deterministic_ordering() = runTest {
        val dataStreamId1 = DataStreamId(deploymentId1, "device1", dataType1)
        val dataStreamId2 = DataStreamId(deploymentId1, "device2", dataType1)

        val config = DataStreamsConfiguration(
            deploymentId1,
            setOf(
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId1),
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(dataStreamId2)
            )
        )
        service.openDataStreams(config)

        // Add data in different order from different devices
        val batch1 = createTestBatch(deploymentId1, "device1", dataType1, 3000L, 2)
        val batch2 = createTestBatch(deploymentId1, "device2", dataType1, 1000L, 2)

        service.appendToDataStreams(deploymentId1, batch1)
        service.appendToDataStreams(deploymentId1, batch2)

        // Query twice and verify consistent ordering
        val result1 = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = null,
            to = null
        )

        val result2 = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = null,
            to = null
        )

        // Convert to lists of data points for comparison
        val points1 = result1.sequences.flatMap { it }.toList()
        val points2 = result2.sequences.flatMap { it }.toList()

        // Results should be identical
        assertEquals(points1.size, points2.size)
        for (i in points1.indices)
        {
            assertEquals(points1[i].sequenceId, points2[i].sequenceId)
            assertEquals(points1[i].dataStream, points2[i].dataStream)
        }

        // Verify we have the expected number of data points
        assertEquals(4, points1.size) // 2 from each batch

        // Verify ordering and non-overlap per stream
        assertPerStreamOrderAndNonOverlap(result1)
        assertPerStreamOrderAndNonOverlap(result2)
    }


    /**
     * Helper function to create a test batch with sequential timestamps.
     */
    private fun createTestBatch(
        deploymentId: UUID,
        deviceRoleName: String,
        dataType: DataType,
        startTimestamp: Long,
        count: Int
    ): DataStreamBatch
    {
        val dataStreamId = DataStreamId(deploymentId, deviceRoleName, dataType)
        val sequence = MutableDataStreamSequence<NoData>(
            dataStreamId,
            0L,
            listOf(1),
            SyncPoint.UnixEpoch
        )

        val measurements = mutableListOf<Measurement<NoData>>()
        repeat(count) { i ->
            val measurement = Measurement(
                sensorStartTime = startTimestamp + i * 1000L, // 1ms apart in microseconds
                sensorEndTime = null,
                dataType = dataType,
                data = NoData
            )
            measurements.add(measurement)
        }

        sequence.appendMeasurements(measurements)

        val batch = MutableDataStreamBatch()
        batch.appendSequence(sequence)
        return batch
    }

    @Test
    fun getBatchForStudyDeployments_preserves_order_and_non_overlap_per_stream() = runTest {
        val d1 = DataStreamId(deploymentId1, "device1", dataType1)
        val cfg = DataStreamsConfiguration(
            deploymentId1,
            setOf(
            DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(d1)
        )
        )
        service.openDataStreams(cfg)

        // Two appends produce two consecutive sequences in the same stream.
        // First batch: seqIds 0..2 (3 measurements)
        val batchA = createTestBatchWithSequenceId(deploymentId1, "device1", dataType1, 1_000_000L, 3, 0L)
        // Second batch: seqIds 3..4 (2 measurements) - must start after first batch ends
        val batchB = createTestBatchWithSequenceId(deploymentId1, "device1", dataType1, 4_000_000L, 2, 3L)
        service.appendToDataStreams(deploymentId1, batchA)
        service.appendToDataStreams(deploymentId1, batchB)

        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = setOf("device1"),
            dataTypes = setOf(dataType1),
            from = null, to = null
        )

        // Group per stream and verify ordering & non-overlap.
        val byStream = result.sequences.groupBy { it.dataStream }
        byStream.forEach { (_, seqs) ->
            // sorted by sequenceId range start
            val sorted = seqs.toList().sortedBy { it.range.first }
            assertEquals(sorted, seqs.toList())

            // non-overlapping: last.end < next.start
            for (i in 0 until sorted.size - 1)
            {
                val a = sorted[i].range
                val b = sorted[i + 1].range
                assertEquals(
                    a.last + 1,
                    b.first,
                    "Sequences must be immediately consecutive or disjoint without overlap"
                )
            }
        }
    }

    /**
     * Helper function to create a test batch with sequential timestamps and custom firstSequenceId.
     */
    private fun createTestBatchWithSequenceId(
        deploymentId: UUID,
        deviceRoleName: String,
        dataType: DataType,
        startTimestamp: Long,
        count: Int,
        firstSequenceId: Long
    ): DataStreamBatch
    {
        val dataStreamId = DataStreamId(deploymentId, deviceRoleName, dataType)
        val sequence = MutableDataStreamSequence<NoData>(
            dataStreamId,
            firstSequenceId,
            listOf(1),
            SyncPoint.UnixEpoch
        )

        val measurements = mutableListOf<Measurement<NoData>>()
        repeat(count) { i ->
            val measurement = Measurement(
                sensorStartTime = startTimestamp + i * 1000L, // 1ms apart in microseconds
                sensorEndTime = null,
                dataType = dataType,
                data = NoData
            )
            measurements.add(measurement)
        }

        sequence.appendMeasurements(measurements)

        val batch = MutableDataStreamBatch()
        batch.appendSequence(sequence)
        return batch
    }

    @Test
    fun getBatchForStudyDeployments_clipping_shifts_firstSequenceId() = runTest {
        val d1 = DataStreamId(deploymentId1, "device1", dataType1)
        service.openDataStreams(
            DataStreamsConfiguration(
                deploymentId1,
                setOf(
                    DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(d1)
            )
            )
        )
        // Build a single sequence with 5 points, seqIds 0..4
        // Use microsecond timestamps: 1_000_000, 1_001_000, 1_002_000, 1_003_000, 1_004_000 (1000ms, 1001ms, 1002ms, 1003ms, 1004ms)
        val batch = createTestBatch(deploymentId1, "device1", dataType1, 1_000_000L, 5)
        service.appendToDataStreams(deploymentId1, batch)

        // Choose a fromTime that trims the first 2 points.
        // fromTime = 1002ms = 1_002_000 microseconds, so it keeps measurements at indices 2, 3, 4
        val fromTime = Instant.fromEpochMilliseconds(1_002L)
        val toTime = null

        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = fromTime, to = toTime
        )

        // Expect exactly one clipped sequence starting at firstSequenceId = 2
        val seqs = result.sequences.toList()
        assertEquals(1, seqs.size)
        assertEquals(2L, seqs[0].firstSequenceId)
        assertEquals(3, seqs[0].measurements.size) // ids 2,3,4 remain

        assertPerStreamOrderAndNonOverlap(result)
    }

    private fun createNonMonotonicBatch(
        deploymentId: UUID,
        dataType: DataType
    ): DataStreamBatch
    {
        val id = DataStreamId(deploymentId, "device1", dataType)
        val seq = MutableDataStreamSequence<NoData>(id, 0L, listOf(1), SyncPoint.UnixEpoch)
        // Deliberately wobble timestamps (microseconds)
        // Use microsecond values that correspond to 1001ms, 1000ms, 1003ms, 1002.5ms, 1004ms
        val base = 1_000_000L // 1000 milliseconds in microseconds
        val ts = listOf(base + 1000, base + 0, base + 3000, base + 2500, base + 4000)
        val ms = ts.map { t ->
            Measurement(sensorStartTime = t, sensorEndTime = null, dataType = dataType, data = NoData)
        }
        seq.appendMeasurements(ms)
        return MutableDataStreamBatch().apply { appendSequence(seq) }
    }

    @Test
    fun getBatchForStudyDeployments_handles_non_monotonic_timestamps() = runTest {
        val id = DataStreamId(deploymentId1, "device1", dataType1)
        service.openDataStreams(
            DataStreamsConfiguration(
                deploymentId1,
                setOf(
                DataStreamsConfiguration.ExpectedDataStream.fromDataStreamId(id)
            )
            )
        )
        service.appendToDataStreams(deploymentId1, createNonMonotonicBatch(deploymentId1, dataType1))

        // Select a middle window that should pick a contiguous chunk by index (chunking logic)
        val fromTime = Instant.fromEpochMilliseconds(1_002L)
        val toTime = Instant.fromEpochMilliseconds(1_004L)

        val result = service.getBatchForStudyDeployments(
            studyDeploymentIds = setOf(deploymentId1),
            deviceRoleNames = null,
            dataTypes = null,
            from = fromTime, to = toTime
        )

        val seqs = result.sequences.toList()
        assertTrue(seqs.isNotEmpty())

        assertPerStreamOrderAndNonOverlap(result)
    }

    private fun assertPerStreamOrderAndNonOverlap( batch: DataStreamBatch )
    {
        val byStream = batch.sequences.groupBy { it.dataStream }
        byStream.forEach {
            (_, seqs) ->
            val sorted = seqs.toList().sortedBy { it.range.first }
            assertEquals(sorted, seqs.toList())
            for (i in 0 until sorted.size - 1)
            {
                val a = sorted[i].range
                val b = sorted[i + 1].range
                assertTrue(a.last + 1 <= b.first, "Overlapping sequences in stream")
            }
        }
    }
}
