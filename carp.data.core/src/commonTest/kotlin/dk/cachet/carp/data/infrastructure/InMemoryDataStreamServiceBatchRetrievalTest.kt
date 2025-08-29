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
                assertTrue(absoluteStartTime in fromTime..toTime)
            }
        }
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
}
