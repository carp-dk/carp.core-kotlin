package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlannedStepTest
{

    @Test
    fun `serialization round-trip preserves CommandRun planned step`()
    {
        val inputBindingId = UUID.randomUUID()
        val outputBindingId = UUID.randomUUID()
        val dataRefId = UUID.randomUUID()
        val dataSinkRefId = UUID.randomUUID()

        val step = PlannedStep(
            stepId = UUID.randomUUID(),
            name = "Example Command Step",
            process = CommandSpec(
                executable = "echo",
                args = listOf(ExpandedArg.Literal("hello"))
            ),
            bindings = ResolvedBindings(
                inputs = mapOf(inputBindingId to DataRef(dataRefId, "text/plain")),
                outputs = mapOf(outputBindingId to DataRef(dataSinkRefId, "text/plain"))
            ),
            environmentRef = UUID.randomUUID()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(step)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PlannedStep>(encoded)

        assertEquals(step, decoded)
    }

    @Test
    fun `serialization round-trip preserves InProcessRun planned step`()
    {
        val outputBindingId = UUID.randomUUID()
        val dataSinkRefId = UUID.randomUUID()

        val step = PlannedStep(
            stepId = UUID.randomUUID(),
            name = "Example In-Process Step",
            process = InTasksRun(
                operationId = "analysis.example.v1",
                parameters = mapOf("k" to "v")
            ),
            bindings = ResolvedBindings(
                inputs = emptyMap(),
                outputs = mapOf(outputBindingId to DataRef(dataSinkRefId, "application/json"))
            ),
            environmentRef = UUID.randomUUID()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(step)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PlannedStep>(encoded)

        assertEquals(step, decoded)
    }

    @Test
    fun `constructor validates required fields`()
    {
        assertFailsWith<IllegalArgumentException> {
            PlannedStep(
                stepId = UUID.randomUUID(),
                name = "",
                process = InTasksRun("op"),
                bindings = ResolvedBindings(),
                environmentRef = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `bindings are preserved and accessible`()
    {
        val inputBindingId = UUID.randomUUID()
        val outputBindingId = UUID.randomUUID()
        val dataRefId = UUID.randomUUID()
        val dataSinkRefId = UUID.randomUUID()

        val bindings = ResolvedBindings(
            inputs = mapOf(inputBindingId to DataRef(dataRefId, "t")),
            outputs = mapOf(outputBindingId to DataRef(dataSinkRefId, "t")),
        )

        val step = PlannedStep(
            stepId = UUID.randomUUID(),
            name = "Step",
            process = InTasksRun("op"),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        assertEquals(dataRefId, step.bindings.input(inputBindingId)?.id)
        assertEquals(dataSinkRefId, step.bindings.output(outputBindingId)?.id)
    }
}
