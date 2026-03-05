package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

class TasksRunTest
{

    @Test
    fun `CommandRun serializes and deserializes via ProcessRun`()
    {
        val run: TasksRun = CommandSpec(
            executable = "python",
            args = listOf("-c", "print('hi')")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(run)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<TasksRun>(encoded)

        assertEquals(run, decoded)
    }

    @Test
    fun `CommandRun validates fields`()
    {
        assertFailsWith<IllegalArgumentException> { CommandSpec(executable = "") }
    }


    @Test
    fun `InProcessRun serializes and deserializes via ProcessRun`()
    {
        val run: TasksRun = InTasksRun(
            operationId = "operation.example.v1",
            parameters = mapOf("k" to "v")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(run)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<TasksRun>(encoded)

        assertEquals(run, decoded)
    }

    @Test
    fun `InProcessRun validates fields`()
    {
        assertFailsWith<IllegalArgumentException> { InTasksRun(operationId = "") }
        assertFailsWith<IllegalArgumentException> { InTasksRun(operationId = "   ") }
    }

    @Test
    fun `InProcessRun defensively copies parameters`()
    {
        val params = mutableMapOf("k" to "v1")
        val run = InTasksRun(operationId = "operation", parameters = params)

        assertNotSame(params, run.safeParameters)

        params["k"] = "v2"
        assertEquals(mapOf("k" to "v1"), run.safeParameters)
    }
}
