package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Tests for [ExecutionStatus] serialization roundtrip compatibility.
 */
class ExecutionStatusTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_ExecutionStatus()
    {
        val statuses = ExecutionStatus.entries.toList()

        statuses.forEach { status ->
            val serialized = json.encodeToString(status)
            val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
            assertEquals(status, deserialized)
        }
    }

    @Test
    fun serialization_preserves_all_ExecutionStatus_values()
    {
        val allStatuses = ExecutionStatus.entries.toList()

        val serialized = json.encodeToString(allStatuses)
        val deserialized = json.decodeFromString<List<ExecutionStatus>>(serialized)

        assertEquals(allStatuses, deserialized)
    }

    @Test
    fun can_serialize_PENDING()
    {
        val status = ExecutionStatus.PENDING
        val serialized = json.encodeToString(status)
        assertTrue(serialized.contains("PENDING"))
        val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
        assertEquals(status, deserialized)
    }

    @Test
    fun can_serialize_RUNNING()
    {
        val status = ExecutionStatus.RUNNING
        val serialized = json.encodeToString(status)
        assertTrue(serialized.contains("RUNNING"))
        val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
        assertEquals(status, deserialized)
    }

    @Test
    fun can_serialize_SUCCEEDED()
    {
        val status = ExecutionStatus.SUCCEEDED
        val serialized = json.encodeToString(status)
        assertTrue(serialized.contains("SUCCEEDED"))
        val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
        assertEquals(status, deserialized)
    }

    @Test
    fun can_serialize_FAILED()
    {
        val status = ExecutionStatus.FAILED
        val serialized = json.encodeToString(status)
        assertTrue(serialized.contains("FAILED"))
        val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
        assertEquals(status, deserialized)
    }

    @Test
    fun can_serialize_SKIPPED()
    {
        val status = ExecutionStatus.SKIPPED
        val serialized = json.encodeToString(status)
        assertTrue(serialized.contains("SKIPPED"))
        val deserialized = json.decodeFromString<ExecutionStatus>(serialized)
        assertEquals(status, deserialized)
    }
}
