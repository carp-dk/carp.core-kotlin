package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for [RTaskDefinition] and [RScript].
 *
 * Tests cover construction validation, interface compliance, serialization round-trips,
 * and polymorphic dispatch.  No runtime/execution behaviour is tested — this class is
 * purely declarative.
 */
class RTaskDefinitionTest
{

    // REntryPoint — RScript

    @Test
    fun `RScript creates valid instance`()
    {
        val ep = RScript("scripts/preprocess.R")
        assertEquals("scripts/preprocess.R", ep.scriptPath)
    }

    @Test
    fun `RScript rejects blank scriptPath`()
    {
        assertFailsWith<IllegalArgumentException> { RScript("") }
        assertFailsWith<IllegalArgumentException> { RScript("   ") }
    }

    @Test
    fun `RScript serialization round-trip`()
    {
        val original = RScript("pipeline/run.R")
        val encoded = CoreAnalyticsSerializer.json.encodeToString(original as REntryPoint)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<REntryPoint>(encoded)
        assertEquals(original, decoded)
        assertTrue(decoded is RScript)
    }

    @Test
    fun `RScript SerialName is correct`()
    {
        val encoded = CoreAnalyticsSerializer.json.encodeToString(RScript("run.R") as REntryPoint)
        assertTrue(encoded.contains("\"type\":\"RScript\""), "Expected 'RScript' discriminator, got: $encoded")
    }

    // RTaskDefinition — construction

    @Test
    fun `creates minimal RTaskDefinition with RScript entry point`()
    {
        val id = UUID.randomUUID()
        val task = RTaskDefinition(
            id = id,
            name = "preprocess",
            entryPoint = RScript("scripts/preprocess.R")
        )

        assertEquals(id, task.id)
        assertEquals("preprocess", task.name)
        assertNull(task.description)
        assertEquals("scripts/preprocess.R", task.entryPoint.scriptPath)
        assertEquals(emptyList(), task.args)
    }

    @Test
    fun `creates full RTaskDefinition with all fields`()
    {
        val id = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val args = listOf(
            Literal("--input"),
            InputRef(inputId),
            Literal("--output"),
            OutputRef(outputId),
            ParamRef("iterations")
        )

        val task = RTaskDefinition(
            id = id,
            name = "full-r-task",
            description = "Runs the full R pipeline",
            entryPoint = RScript("pipeline/run.R"),
            args = args,
        )

        assertEquals(id, task.id)
        assertEquals("full-r-task", task.name)
        assertEquals("Runs the full R pipeline", task.description)
        assertEquals(RScript("pipeline/run.R"), task.entryPoint)
        assertEquals(args, task.args)
    }

    @Test
    fun `entryPoint is always RScript (not a sealed hierarchy)`()
    {
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "r-task",
            entryPoint = RScript("analysis.R")
        )

        assertEquals("analysis.R", task.entryPoint.scriptPath)
    }

    // RTaskDefinition — validation

    @Test
    fun `rejects blank name`()
    {
        assertFailsWith<IllegalArgumentException> {
            RTaskDefinition(
                id = UUID.randomUUID(),
                name = "",
                entryPoint = RScript("run.R")
            )
        }

        assertFailsWith<IllegalArgumentException> {
            RTaskDefinition(
                id = UUID.randomUUID(),
                name = "   ",
                entryPoint = RScript("analysis.R")
            )
        }
    }

    // RTaskDefinition — TaskDefinition interface

    @Test
    fun `implements TaskDefinition interface correctly`()
    {
        val id = UUID.randomUUID()
        val task = RTaskDefinition(
            id = id,
            name = "interface-test",
            entryPoint = RScript("test.R")
        )

        val taskDef: TaskDefinition = task
        assertEquals(id, taskDef.id)
        assertEquals("interface-test", taskDef.name)
        assertNull(taskDef.description)
    }

    // RTaskDefinition — serialization

    @Test
    fun `serialization round-trip with RScript entry point`()
    {
        val original = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "serialize-rscript",
            description = "Test R script serialization",
            entryPoint = RScript("scripts/run.R"),
            args = listOf(Literal("--verbose"), InputRef(UUID.randomUUID())),
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<RTaskDefinition>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `serialization preserves null description`()
    {
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "null-fields",
            entryPoint = RScript("run.R")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<RTaskDefinition>(encoded)

        assertEquals(task, decoded)
        assertNull(decoded.description)
    }

    @Test
    fun `serialization preserves empty args list`()
    {
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "empty-args",
            entryPoint = RScript("analysis.R"),
            args = emptyList()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<RTaskDefinition>(encoded)

        assertEquals(emptyList(), decoded.args)
    }

    @Test
    fun `serialization preserves complex arg tokens`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "complex-args",
            entryPoint = RScript("pipeline.R"),
            args = listOf(
                Literal("--config"),
                Literal("config.yml"),
                InputRef(inputId),
                OutputRef(outputId),
                ParamRef("threshold"),
                Literal("--mode=analysis")
            )
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<RTaskDefinition>(encoded)

        assertEquals(task, decoded)
        assertEquals(6, decoded.args.size)
    }

    @Test
    fun `polymorphic serialization as TaskDefinition works`()
    {
        val original: TaskDefinition = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "polymorphic-test",
            entryPoint = RScript("run.R")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<TaskDefinition>(encoded)

        assertEquals(original, decoded)
        assertTrue(decoded is RTaskDefinition)
    }

    @Test
    fun `SerialName annotation produces correct type discriminator`()
    {
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "discriminator-test",
            entryPoint = RScript("analysis.R")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString<TaskDefinition>(task)
        assertTrue(
            encoded.contains("\"type\":\"RTaskDefinition\""),
            "Expected 'RTaskDefinition' discriminator, got: $encoded"
        )
    }

    // RTaskDefinition — equality and hashing

    @Test
    fun `two RTaskDefinitions with same fields are equal`()
    {
        val id = UUID.randomUUID()
        val ep = RScript("run.R")
        val args = listOf(Literal("--verbose"))

        val task1 = RTaskDefinition(id = id, name = "test", entryPoint = ep, args = args)
        val task2 = RTaskDefinition(id = id, name = "test", entryPoint = ep, args = args)

        assertEquals(task1, task2)
        assertEquals(task1.hashCode(), task2.hashCode())
    }

    @Test
    fun `RTaskDefinitions with different ids are not equal`()
    {
        val ep = RScript("run.R")
        val task1 = RTaskDefinition(id = UUID.randomUUID(), name = "test", entryPoint = ep)
        val task2 = RTaskDefinition(id = UUID.randomUUID(), name = "test", entryPoint = ep)

        assertNotEquals(task1, task2)
    }

    @Test
    fun `RTaskDefinitions with different entryPoints are not equal`()
    {
        val id = UUID.randomUUID()
        val task1 = RTaskDefinition(id = id, name = "test", entryPoint = RScript("run1.R"))
        val task2 = RTaskDefinition(id = id, name = "test", entryPoint = RScript("run2.R"))

        assertNotEquals(task1, task2)
    }

    @Test
    fun `RScript with different paths are not equal`()
    {
        val script1 = RScript("analysis.R")
        val script2 = RScript("other.R")

        assertNotEquals(script1, script2)
        assertNotEquals(script1.hashCode(), script2.hashCode())
    }

    // RTaskDefinition — no execution-related concerns

    @Test
    fun `task definition carries no executable field`()
    {
        // RTaskDefinition deliberately has no 'executable' property.
        // The executable is resolved at plan-time by the planner using the environment.
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "no-executable",
            entryPoint = RScript("run.R")
        )

        // Verify only declarative fields exist (compile-time assertion via property access)
        assertNotNull(task.id)
        assertNotNull(task.name)
        assertNotNull(task.entryPoint)
        assertNotNull(task.args)
        // task.executable would be a compile error — intentional omission
    }

    // Integration with other ArgToken types

    @Test
    fun `RTaskDefinition works with all ArgToken variants`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "all-tokens",
            entryPoint = RScript("process.R"),
            args = listOf(
                Literal("input-file.csv"),
                InputRef(inputId),
                OutputRef(outputId),
                ParamRef("batch_size"),
                Literal("--verbose")
            )
        )

        assertEquals(5, task.args.size)
        assertTrue(task.args[0] is Literal)
        assertTrue(task.args[1] is InputRef)
        assertTrue(task.args[2] is OutputRef)
        assertTrue(task.args[3] is ParamRef)
        assertTrue(task.args[4] is Literal)
    }

    @Test
    fun `toString provides readable output`()
    {
        val task = RTaskDefinition(
            id = UUID.randomUUID(),
            name = "my-r-task",
            entryPoint = RScript("analysis.R"),
            description = "Analyzes data"
        )

        val str = task.toString()
        assertTrue(str.contains("RTaskDefinition"))
        assertTrue(str.contains("my-r-task"))
        assertTrue(str.contains("analysis.R"))
    }
}


// Helper function for inequality assertions
private fun <T> assertNotEquals( expected: T, actual: T, message: String? = null )
{
    if (expected == actual)
    {
        throw AssertionError(message ?: "Expected values to be different, but both are: $expected")
    }
}

