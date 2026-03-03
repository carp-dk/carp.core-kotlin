package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Tests for [StepRunResult] serialization roundtrip compatibility.
 */
class StepRunResultTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_StepRunResult_successful()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SUCCEEDED,
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T10:05:00Z"),
            failure = null,
            outputs = listOf(
                ProducedOutputRef(
                    outputId = UUID.randomUUID(),
                    location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step-123/outputs/result.json"),
                    sizeBytes = 1024L,
                    contentType = "application/json"
                )
            ),
            detail = StepRunDetail(
                command = listOf("echo", "hello world"),
                exitCode = 0,
                metrics = mapOf("duration" to 300000.0)
            )
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_with_failure()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.FAILED,
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T10:01:00Z"),
            failure = StepFailure(
                kind = FailureKind.TIMEOUT,
                message = "Operation timed out after 60 seconds"
            ),
            outputs = emptyList(),
            detail = StepRunDetail(
                command = listOf("long-running-command"),
                exitCode = 124
            )
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_minimal()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.PENDING,
            startedAt = null,
            finishedAt = null,
            failure = null,
            outputs = null,
            detail = null
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_with_empty_outputs()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SUCCEEDED,
            startedAt = Instant.parse("2026-03-02T09:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T09:05:00Z"),
            failure = null,
            outputs = emptyList(),
            detail = StepRunDetail(exitCode = 0)
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertTrue(deserialized.outputs?.isEmpty() == true)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_with_multiple_outputs()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SUCCEEDED,
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T11:00:00Z"),
            failure = null,
            outputs = listOf(
                ProducedOutputRef(
                    outputId = UUID.randomUUID(),
                    location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step-456/outputs/results1.csv"),
                    sizeBytes = 2048L,
                    contentType = "text/csv"
                ),
                ProducedOutputRef(
                    outputId = UUID.randomUUID(),
                    location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step-456/outputs/results2.json"),
                    sizeBytes = 1536L,
                    contentType = "application/json"
                ),
                ProducedOutputRef(
                    outputId = UUID.randomUUID(),
                    location = ResourceRef(ResourceKind.URI, "s3://bucket/processed_data.parquet"),
                    sizeBytes = 10240L,
                    sha256 = "abc123def456",
                    contentType = "application/octet-stream"
                )
            )
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertEquals(3, deserialized.outputs?.size)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_running()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.RUNNING,
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = null,
            failure = null,
            outputs = null,
            detail = StepRunDetail(
                command = listOf("python", "long_script.py"),
                metrics = mapOf("progress_percent" to 45.0)
            )
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertNull(deserialized.finishedAt)
        assertEquals(ExecutionStatus.RUNNING, deserialized.status)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_skipped()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SKIPPED,
            startedAt = null,
            finishedAt = null,
            failure = null,
            outputs = emptyList()
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertEquals(ExecutionStatus.SKIPPED, deserialized.status)
        assertNull(deserialized.startedAt)
        assertNull(deserialized.finishedAt)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_with_all_failure_kinds()
    {
        val failureKinds = FailureKind.entries.toTypedArray()

        failureKinds.forEach { failureKind ->
            val stepRunResult = StepRunResult(
                stepId = UUID.randomUUID(),
                status = ExecutionStatus.FAILED,
                startedAt = Instant.parse("2026-03-02T10:00:00Z"),
                finishedAt = Instant.parse("2026-03-02T10:01:00Z"),
                failure = StepFailure(failureKind, "Test failure for $failureKind"),
                outputs = emptyList()
            )

            val serialized = json.encodeToString(stepRunResult)
            val deserialized = json.decodeFromString<StepRunResult>(serialized)

            assertEquals(stepRunResult, deserialized)
            assertEquals(failureKind, deserialized.failure!!.kind)
        }
    }

    @Test
    fun can_serialize_and_deserialize_StepRunResult_with_complex_detail()
    {
        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SUCCEEDED,
            startedAt = Instant.parse("2026-03-02T08:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T08:30:00Z"),
            failure = null,
            outputs = listOf(
                ProducedOutputRef(
                    outputId = UUID.randomUUID(),
                    location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step-789/outputs/processed.json"),
                    sizeBytes = 4096L,
                    sha256 = "def789ghi012",
                    contentType = "application/json"
                )
            ),
            detail = StepRunDetail(
                command = listOf("python", "data_processor.py", "--input", "raw_data.csv", "--output", "processed"),
                workingDirectory = "analytics/workflows/data-processing",
                exitCode = 0,
                stdout = ResourceRef(ResourceKind.URI, "s3://logs/stdout/run123.txt", "text/plain", 2048L),
                stderr = ResourceRef(ResourceKind.RELATIVE_PATH, "logs/stderr.log"),
                log = ResourceRef(ResourceKind.URI, "https://logs.internal.com/run123"),
                metrics = mapOf(
                    "rows_processed" to 50000.0,
                    "processing_time_ms" to 1800000.0,
                    "memory_peak_mb" to 512.0,
                    "disk_io_mb" to 1024.0
                )
            )
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertEquals(4, deserialized.detail!!.metrics!!.size)
        assertEquals(50000.0, deserialized.detail.metrics["rows_processed"])
    }

    @Test
    fun serialization_preserves_precise_timestamps()
    {
        val startTime = Instant.parse("2026-03-02T10:15:30.123456789Z")
        val finishTime = Instant.parse("2026-03-02T10:20:45.987654321Z")

        val stepRunResult = StepRunResult(
            stepId = UUID.randomUUID(),
            status = ExecutionStatus.SUCCEEDED,
            startedAt = startTime,
            finishedAt = finishTime,
            failure = null,
            outputs = emptyList()
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(stepRunResult, deserialized)
        assertEquals(startTime, deserialized.startedAt)
        assertEquals(finishTime, deserialized.finishedAt)
    }

    @Test
    fun serialization_preserves_uuid_precision()
    {
        val specificUuid = UUID.parse("550e8400-e29b-41d4-a716-446655440000")
        val stepRunResult = StepRunResult(
            stepId = specificUuid,
            status = ExecutionStatus.SUCCEEDED,
            startedAt = null,
            finishedAt = null,
            failure = null,
            outputs = emptyList()
        )

        val serialized = json.encodeToString(stepRunResult)
        val deserialized = json.decodeFromString<StepRunResult>(serialized)

        assertEquals(specificUuid, deserialized.stepId)
        assertEquals(stepRunResult, deserialized)
    }
}
