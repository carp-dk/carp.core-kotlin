package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds


/**
 * Tests for [ExecutionReport] serialization roundtrip compatibility.
 */
class ExecutionReportTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_successful()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T09:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T10:30:00Z"),
            status = ExecutionStatus.SUCCEEDED,
            stepResults = listOf(
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SUCCEEDED,
                    startedAt = Instant.parse("2026-03-02T09:00:00Z"),
                    finishedAt = Instant.parse("2026-03-02T09:15:00Z"),
                    failure = null,
                    outputs = listOf(
                        ProducedOutputRef(
                            outputId = UUID.randomUUID(),
                            location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step1/outputs/step1_output.csv"),
                            sizeBytes = 2048L,
                            contentType = "text/csv"
                        )
                    )
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SUCCEEDED,
                    startedAt = Instant.parse("2026-03-02T09:15:00Z"),
                    finishedAt = Instant.parse("2026-03-02T10:30:00Z"),
                    failure = null,
                    outputs = listOf(
                        ProducedOutputRef(
                            outputId = UUID.randomUUID(),
                            location = ResourceRef(ResourceKind.URI, "http://results.example.com/final.json"),
                            sizeBytes = 1024L,
                            contentType = "application/json"
                        )
                    )
                )
            )
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_with_failures()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T09:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T09:05:00Z"),
            status = ExecutionStatus.FAILED,
            stepResults = listOf(
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SUCCEEDED,
                    startedAt = Instant.parse("2026-03-02T09:00:00Z"),
                    finishedAt = Instant.parse("2026-03-02T09:02:00Z"),
                    failure = null,
                    outputs = emptyList()
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.FAILED,
                    startedAt = Instant.parse("2026-03-02T09:02:00Z"),
                    finishedAt = Instant.parse("2026-03-02T09:05:00Z"),
                    failure = StepFailure(FailureKind.INFRASTRUCTURE, "Network connection lost"),
                    outputs = emptyList(),
                    detail = StepRunDetail(
                        command = listOf("curl", "http://unavailable-service.com/data"),
                        exitCode = 7
                    )
                )
            )
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_minimal()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = null,
            finishedAt = null,
            status = ExecutionStatus.PENDING,
            stepResults = emptyList()
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_running()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId =UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T08:00:00Z"),
            finishedAt = null,
            status = ExecutionStatus.RUNNING,
            stepResults = listOf(
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SUCCEEDED,
                    startedAt = Instant.parse("2026-03-02T08:00:00Z"),
                    finishedAt = Instant.parse("2026-03-02T08:30:00Z"),
                    failure = null,
                    outputs = listOf(
                        ProducedOutputRef(
                            outputId = UUID.randomUUID(),
                            location = ResourceRef(ResourceKind.URI, "postgresql://user@host:5432/db/processed_data"),
                            sizeBytes = 4096L,
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
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.RUNNING,
                    startedAt = Instant.parse("2026-03-02T08:30:00Z"),
                    finishedAt = null,
                    failure = null,
                    outputs = null,
                    detail = StepRunDetail(
                        command = listOf("spark-submit", "--class", "com.example.Analytics", "analytics.jar"),
                        workingDirectory = "analytics/spark",
                        exitCode = null,
                        metrics = mapOf("progress_percent" to 45.0)
                    )
                )
            )
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
        assertEquals(ExecutionStatus.RUNNING, deserialized.status)
        assertNull(deserialized.finishedAt)
        assertEquals(2, deserialized.stepResults.size)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_with_mixed_step_statuses()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = null,
            status = ExecutionStatus.RUNNING,
            stepResults = listOf(
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SUCCEEDED,
                    startedAt = Instant.parse("2026-03-02T10:00:00Z"),
                    finishedAt = Instant.parse("2026-03-02T10:05:00Z"),
                    failure = null,
                    outputs = emptyList()
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.RUNNING,
                    startedAt = Instant.parse("2026-03-02T10:05:00Z"),
                    finishedAt = null,
                    failure = null,
                    outputs = null
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.PENDING,
                    startedAt = null,
                    finishedAt = null,
                    failure = null,
                    outputs = null
                ),
                StepRunResult(
                    stepId = UUID.randomUUID(),
                    status = ExecutionStatus.SKIPPED,
                    startedAt = null,
                    finishedAt = null,
                    failure = null,
                    outputs = emptyList()
                )
            )
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
        assertEquals(4, deserialized.stepResults.size)
        assertEquals(ExecutionStatus.SUCCEEDED, deserialized.stepResults[0].status)
        assertEquals(ExecutionStatus.RUNNING, deserialized.stepResults[1].status)
        assertEquals(ExecutionStatus.PENDING, deserialized.stepResults[2].status)
        assertEquals(ExecutionStatus.SKIPPED, deserialized.stepResults[3].status)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_with_no_steps()
    {
        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = Instant.parse("2026-03-02T10:00:01Z"),
            status = ExecutionStatus.SUCCEEDED,
            stepResults = emptyList()
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
        assertTrue(deserialized.stepResults.isEmpty())
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_with_large_number_of_steps()
    {
        val steps = (1..100).map { i ->
            StepRunResult(
                stepId = UUID.randomUUID(),
                status = if (i <= 90) ExecutionStatus.SUCCEEDED else ExecutionStatus.RUNNING,
                startedAt = Instant.parse("2026-03-02T10:00:00Z").plus(i.seconds),
                finishedAt = if (i <= 90) Instant.parse("2026-03-02T10:00:00Z").plus((i + 1).seconds) else null,
                failure = null,
                outputs = if (i % 10 == 0) listOf(
                    ProducedOutputRef(
                        outputId = UUID.randomUUID(),
                        location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/step$i/outputs/step$i.json"),
                        sizeBytes = 1024L,
                        contentType = "application/json"
                    )
                ) else emptyList()
            )
        }

        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = Instant.parse("2026-03-02T10:00:00Z"),
            finishedAt = null,
            status = ExecutionStatus.RUNNING,
            stepResults = steps
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
        assertEquals(100, deserialized.stepResults.size)
        assertEquals(10, deserialized.stepResults.count { it.outputs?.isNotEmpty() == true })
    }

    @Test
    fun serialization_preserves_precise_timestamps()
    {
        val startedAt = Instant.parse("2026-03-02T10:15:30.123456789Z")
        val finishedAt = Instant.parse("2026-03-02T10:20:45.987654321Z")

        val executionReport = ExecutionReport(
            runId = UUID.randomUUID(),
            planId = UUID.randomUUID(),
            startedAt = startedAt,
            finishedAt = finishedAt,
            status = ExecutionStatus.SUCCEEDED,
            stepResults = emptyList()
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(executionReport, deserialized)
        assertEquals(startedAt, deserialized.startedAt)
        assertEquals(finishedAt, deserialized.finishedAt)
    }

    @Test
    fun serialization_preserves_uuid_precision()
    {
        val specificUuid = UUID.parse("550e8400-e29b-41d4-a716-446655440000")
        val executionReport = ExecutionReport(
            runId = specificUuid,
            planId = UUID.randomUUID(),
            startedAt = null,
            finishedAt = null,
            status = ExecutionStatus.SUCCEEDED,
            stepResults = emptyList()
        )

        val serialized = json.encodeToString(executionReport)
        val deserialized = json.decodeFromString<ExecutionReport>(serialized)

        assertEquals(specificUuid, deserialized.runId)
        assertEquals(executionReport, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_ExecutionReport_with_all_execution_statuses()
    {
        ExecutionStatus.entries.forEach { status ->
            val executionReport = ExecutionReport(
                runId = UUID.randomUUID(),
                planId = UUID.randomUUID(),
                startedAt = if (status != ExecutionStatus.PENDING) Instant.parse("2026-03-02T10:00:00Z") else null,
                finishedAt = if (status in listOf(ExecutionStatus.SUCCEEDED, ExecutionStatus.FAILED, ExecutionStatus.SKIPPED))
                    Instant.parse("2026-03-02T10:01:00Z") else null,
                status = status,
                stepResults = emptyList()
            )

            val serialized = json.encodeToString(executionReport)
            val deserialized = json.decodeFromString<ExecutionReport>(serialized)

            assertEquals(executionReport, deserialized)
            assertEquals(status, deserialized.status)
        }
    }
}
