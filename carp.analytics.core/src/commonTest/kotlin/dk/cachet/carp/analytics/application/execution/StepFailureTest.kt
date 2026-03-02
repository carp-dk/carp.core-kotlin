package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.infrastructure.test.createTestJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Tests for [StepFailure] and [FailureKind] serialization roundtrip compatibility.
 */
class StepFailureTest
{
    private val json = createTestJSON()

    @Test
    fun can_serialize_and_deserialize_FailureKind()
    {
        val failureKinds = FailureKind.entries.toList()

        failureKinds.forEach { kind ->
            val serialized = json.encodeToString(kind)
            val deserialized = json.decodeFromString<FailureKind>(serialized)
            assertEquals(kind, deserialized)
        }
    }

    @Test
    fun can_serialize_and_deserialize_StepFailure()
    {
        val failure = StepFailure(
            kind = FailureKind.COMMAND_FAILED,
            message = "Test failure message"
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
    }

    @Test
    fun can_serialize_StepFailure_with_TIMEOUT()
    {
        val failure = StepFailure(
            kind = FailureKind.TIMEOUT,
            message = "Operation timed out after 60 seconds"
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
        assertTrue(serialized.contains("TIMEOUT"))
        assertTrue(serialized.contains("Operation timed out"))
    }

    @Test
    fun can_serialize_StepFailure_with_CANCELLED()
    {
        val failure = StepFailure(
            kind = FailureKind.CANCELLED,
            message = "User cancelled the operation"
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
    }

    @Test
    fun can_serialize_StepFailure_with_INFRASTRUCTURE()
    {
        val failure = StepFailure(
            kind = FailureKind.INFRASTRUCTURE,
            message = "Network connection lost"
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
    }

    @Test
    fun can_serialize_StepFailure_with_UNKNOWN()
    {
        val failure = StepFailure(
            kind = FailureKind.UNKNOWN,
            message = "Unexpected error occurred"
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
    }

    @Test
    fun serialization_preserves_all_FailureKind_values()
    {
        val allFailureKinds = FailureKind.entries.toList()

        val serialized = json.encodeToString(allFailureKinds)
        val deserialized = json.decodeFromString<List<FailureKind>>(serialized)

        assertEquals(allFailureKinds, deserialized)
    }

    @Test
    fun can_serialize_StepFailure_with_empty_message()
    {
        val failure = StepFailure(
            kind = FailureKind.COMMAND_FAILED,
            message = ""
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
    }

    @Test
    fun can_serialize_StepFailure_with_long_message()
    {
        val longMessage = "This is a very long error message that contains detailed information about what went wrong during the execution of the step. It includes technical details, stack traces, and other diagnostic information that might be useful for debugging purposes."
        val failure = StepFailure(
            kind = FailureKind.COMMAND_FAILED,
            message = longMessage
        )

        val serialized = json.encodeToString(failure)
        val deserialized = json.decodeFromString<StepFailure>(serialized)

        assertEquals(failure, deserialized)
        assertEquals(longMessage, deserialized.message)
    }
}
