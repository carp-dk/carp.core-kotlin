package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Tests for [StepRunDetail] serialization roundtrip compatibility.
 */
class StepRunDetailTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_StepRunDetail_with_all_fields()
    {
        val detail = StepRunDetail(
            command = listOf("python", "script.py", "--arg1", "value1"),
            workingDirectory = "workspace/project",
            exitCode = 0,
            stdout = ResourceRef(ResourceKind.RELATIVE_PATH, "logs/stdout.txt", "text/plain", 512L),
            stderr = ResourceRef(ResourceKind.RELATIVE_PATH, "logs/stderr.txt", "text/plain", 128L),
            log = ResourceRef(ResourceKind.URI, "http://logs.example.com/run123", "text/plain"),
            metrics = mapOf(
                "execution_time_ms" to 1500.0,
                "memory_usage_mb" to 256.5,
                "cpu_utilization_percent" to 75.2
            )
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
    }

    @Test
    fun can_serialize_and_deserialize_StepRunDetail_with_minimal_data()
    {
        val detail = StepRunDetail()

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertNull(deserialized.command)
        assertNull(deserialized.workingDirectory)
        assertNull(deserialized.exitCode)
        assertNull(deserialized.stdout)
        assertNull(deserialized.stderr)
        assertNull(deserialized.log)
        assertNull(deserialized.metrics)
    }

    @Test
    fun can_serialize_StepRunDetail_with_only_command()
    {
        val detail = StepRunDetail(
            command = listOf("echo", "hello world")
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals(listOf("echo", "hello world"), deserialized.command)
    }

    @Test
    fun can_serialize_StepRunDetail_with_complex_command()
    {
        val detail = StepRunDetail(
            command = listOf(
                "docker", "run", "--rm", "-v", "/data:/app/data",
                "analytics:latest", "python", "process.py", "--input", "/app/data/input.csv"
            ),
            workingDirectory = "/app",
            exitCode = 0
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertTrue(deserialized.command!!.contains("docker"))
        assertTrue(deserialized.command.contains("analytics:latest"))
    }

    @Test
    fun can_serialize_StepRunDetail_with_metrics_only()
    {
        val detail = StepRunDetail(
            metrics = mapOf(
                "rows_processed" to 50000.0,
                "processing_time_ms" to 1800000.0,
                "memory_peak_mb" to 512.0,
                "disk_io_mb" to 1024.0
            )
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals(50000.0, deserialized.metrics!!["rows_processed"])
        assertEquals(1800000.0, deserialized.metrics["processing_time_ms"])
        assertEquals(512.0, deserialized.metrics["memory_peak_mb"])
        assertEquals(1024.0, deserialized.metrics["disk_io_mb"])
    }

    @Test
    fun can_serialize_StepRunDetail_with_failure_exit_code()
    {
        val detail = StepRunDetail(
            command = listOf("failing-command"),
            exitCode = 1,
            stderr = ResourceRef(ResourceKind.RELATIVE_PATH, "error.log")
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals(1, deserialized.exitCode)
    }

    @Test
    fun can_serialize_StepRunDetail_with_all_resource_refs()
    {
        val detail = StepRunDetail(
            stdout = ResourceRef(ResourceKind.URI, "s3://logs/stdout/run123.txt", "text/plain", 2048L),
            stderr = ResourceRef(ResourceKind.RELATIVE_PATH, "logs/stderr.log"),
            log = ResourceRef(ResourceKind.URI, "https://logs.internal.com/run123")
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals("s3://logs/stdout/run123.txt", deserialized.stdout!!.value)
        assertEquals("logs/stderr.log", deserialized.stderr!!.value)
        assertEquals("https://logs.internal.com/run123", deserialized.log!!.value)
    }

    @Test
    fun can_serialize_StepRunDetail_with_empty_metrics()
    {
        val detail = StepRunDetail(
            metrics = emptyMap()
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertTrue(deserialized.metrics!!.isEmpty())
    }

    @Test
    fun can_serialize_StepRunDetail_with_relative_working_directory()
    {
        val detail = StepRunDetail(
            workingDirectory = "../parent/project",
            command = listOf("make", "build")
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals("../parent/project", deserialized.workingDirectory)
    }

    @Test
    fun can_serialize_StepRunDetail_with_single_metric()
    {
        val detail = StepRunDetail(
            metrics = mapOf("duration" to 300000.0)
        )

        val serialized = json.encodeToString(detail)
        val deserialized = json.decodeFromString<StepRunDetail>(serialized)

        assertEquals(detail, deserialized)
        assertEquals(1, deserialized.metrics!!.size)
        assertEquals(300000.0, deserialized.metrics["duration"])
    }
}
