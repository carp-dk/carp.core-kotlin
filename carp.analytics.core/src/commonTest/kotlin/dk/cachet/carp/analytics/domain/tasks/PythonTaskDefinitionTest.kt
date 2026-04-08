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
 * Test suite for [PythonTaskDefinition], [Script], and [Module].
 *
 * Tests cover construction validation, interface compliance, serialization round-trips,
 * and polymorphic dispatch.  No runtime/execution behaviour is tested — this class is
 * purely declarative.
 */
class PythonTaskDefinitionTest
{

    // PythonEntryPoint — Script

    @Test
    fun `Script creates valid instance`()
    {
        val ep = Script("scripts/preprocess.py")
        assertEquals("scripts/preprocess.py", ep.scriptPath)
    }

    @Test
    fun `Script rejects blank scriptPath`()
    {
        assertFailsWith<IllegalArgumentException> { Script("") }
        assertFailsWith<IllegalArgumentException> { Script("   ") }
    }

    @Test
    fun `Script serialization round-trip`()
    {
        val original = Script("pipeline/run.py")
        val encoded = CoreAnalyticsSerializer.json.encodeToString(original as PythonEntryPoint)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonEntryPoint>(encoded)
        assertEquals(original, decoded)
        assertTrue(decoded is Script)
    }

    @Test
    fun `Script SerialName is correct`()
    {
        val encoded = CoreAnalyticsSerializer.json.encodeToString(Script("run.py") as PythonEntryPoint)
        assertTrue(encoded.contains("\"type\":\"Script\""), "Expected 'Script' discriminator, got: $encoded")
    }

    // PythonEntryPoint — Module

    @Test
    fun `Module creates valid instance`()
    {
        val ep = Module("mypackage.cli")
        assertEquals("mypackage.cli", ep.moduleName)
    }

    @Test
    fun `Module rejects blank moduleName`()
    {
        assertFailsWith<IllegalArgumentException> { Module("") }
        assertFailsWith<IllegalArgumentException> { Module("   ") }
    }

    @Test
    fun `Module serialization round-trip`()
    {
        val original = Module("mypackage.cli")
        val encoded = CoreAnalyticsSerializer.json.encodeToString(original as PythonEntryPoint)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonEntryPoint>(encoded)
        assertEquals(original, decoded)
        assertTrue(decoded is Module)
    }

    @Test
    fun `Module SerialName is correct`()
    {
        val encoded = CoreAnalyticsSerializer.json.encodeToString(Module("pkg.main") as PythonEntryPoint)
        assertTrue(encoded.contains("\"type\":\"Module\""), "Expected 'Module' discriminator, got: $encoded")
    }

    // PythonTaskDefinition — construction

    @Test
    fun `creates minimal PythonTaskDefinition with Script entry point`()
    {
        val id = UUID.randomUUID()
        val task = PythonTaskDefinition(
            id = id,
            name = "preprocess",
            entryPoint = Script("scripts/preprocess.py")
        )

        assertEquals(id, task.id)
        assertEquals("preprocess", task.name)
        assertNull(task.description)
        assertTrue(task.entryPoint is Script)
        assertEquals("scripts/preprocess.py", task.entryPoint.scriptPath)
        assertEquals(emptyList(), task.args)
    }

    @Test
    fun `creates minimal PythonTaskDefinition with Module entry point`()
    {
        val id = UUID.randomUUID()
        val task = PythonTaskDefinition(
            id = id,
            name = "run-module",
            entryPoint = Module("mypackage.cli")
        )

        assertEquals(id, task.id)
        assertEquals("run-module", task.name)
        assertTrue(task.entryPoint is Module)
        assertEquals("mypackage.cli", task.entryPoint.moduleName)
    }

    @Test
    fun `creates full PythonTaskDefinition with all fields`()
    {
        val id = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val args = listOf(
            Literal("--input"),
            InputRef(inputId),
            Literal("--output"),
            OutputRef(outputId),
            ParamRef("batch-size")
        )

        val task = PythonTaskDefinition(
            id = id,
            name = "full-python-task",
            description = "Runs the full pipeline",
            entryPoint = Script("pipeline/run.py"),
            args = args,
        )

        assertEquals(id, task.id)
        assertEquals("full-python-task", task.name)
        assertEquals("Runs the full pipeline", task.description)
        assertEquals(Script("pipeline/run.py"), task.entryPoint)
        assertEquals(args, task.args)
    }

    // PythonTaskDefinition — validation

    @Test
    fun `rejects blank name`()
    {
        assertFailsWith<IllegalArgumentException> {
            PythonTaskDefinition(
                id = UUID.randomUUID(),
                name = "",
                entryPoint = Script("run.py")
            )
        }

        assertFailsWith<IllegalArgumentException> {
            PythonTaskDefinition(
                id = UUID.randomUUID(),
                name = "   ",
                entryPoint = Module("pkg")
            )
        }
    }

    // PythonTaskDefinition — TaskDefinition interface

    @Test
    fun `implements TaskDefinition interface correctly`()
    {
        val id = UUID.randomUUID()
        val task = PythonTaskDefinition(
            id = id,
            name = "interface-test",
            entryPoint = Module("pkg.main")
        )

        val taskDef: TaskDefinition = task
        assertEquals(id, taskDef.id)
        assertEquals("interface-test", taskDef.name)
        assertNull(taskDef.description)
    }

    // PythonTaskDefinition — serialization

    @Test
    fun `serialization round-trip with Script entry point`()
    {
        val original = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "serialize-script",
            description = "Test script serialization",
            entryPoint = Script("scripts/run.py"),
            args = listOf(Literal("--verbose"), InputRef(UUID.randomUUID())),
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonTaskDefinition>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `serialization round-trip with Module entry point`()
    {
        val original = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "serialize-module",
            entryPoint = Module("mypackage.cli"),
            args = listOf(ParamRef("epochs"), OutputRef(UUID.randomUUID()))
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonTaskDefinition>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `serialization preserves null description and workingDirectory`()
    {
        val task = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "null-fields",
            entryPoint = Script("run.py")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonTaskDefinition>(encoded)

        assertEquals(task, decoded)
        assertNull(decoded.description)
    }

    @Test
    fun `serialization preserves empty args list`()
    {
        val task = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "empty-args",
            entryPoint = Module("pkg"),
            args = emptyList()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PythonTaskDefinition>(encoded)

        assertEquals(emptyList(), decoded.args)
    }

    @Test
    fun `polymorphic serialization as TaskDefinition works`()
    {
        val original: TaskDefinition = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "polymorphic-test",
            entryPoint = Script("run.py")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<TaskDefinition>(encoded)

        assertEquals(original, decoded)
        assertTrue(decoded is PythonTaskDefinition)
        assertTrue(decoded.entryPoint is Script)
    }

    @Test
    fun `SerialName annotation produces correct type discriminator`()
    {
        val task = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "discriminator-test",
            entryPoint = Module("pkg")
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString<TaskDefinition>(task)
        assertTrue(
            encoded.contains("\"type\":\"PythonTaskDefinition\""),
            "Expected 'PythonTaskDefinition' discriminator, got: $encoded"
        )
    }

    // PythonTaskDefinition — no execution-related concerns

    @Test
    fun `task definition carries no executable field`()
    {
        // PythonTaskDefinition deliberately has no 'executable' property.
        // The executable is resolved at plan-time by the planner using the environment.
        val task = PythonTaskDefinition(
            id = UUID.randomUUID(),
            name = "no-executable",
            entryPoint = Script("run.py")
        )

        // Verify only declarative fields exist (compile-time assertion via property access)
        assertNotNull(task.id)
        assertNotNull(task.name)
        assertNotNull(task.entryPoint)
        assertNotNull(task.args)
        // task.executable would be a compile error — intentional omission
    }
}

