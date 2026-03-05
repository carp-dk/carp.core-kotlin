package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Comprehensive test suite for CommandTaskDefinition.
 *
 * Tests cover validation, builder methods, serialization, and behavior.
 */
class CommandTaskDefinitionTest
{

    @Test
    fun `creates valid CommandTaskDefinition with minimal required fields`()
    {
        val id = UUID.randomUUID()
        val task = CommandTaskDefinition(
            id = id,
            name = "test-command",
            executable = "python"
        )

        assertEquals(id, task.id)
        assertEquals("test-command", task.name)
        assertEquals("python", task.executable)
        assertEquals(null, task.description)
        assertEquals(emptyList(), task.args)
    }

    @Test
    fun `creates valid CommandTaskDefinition with all fields`()
    {
        val id = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val args = listOf(
            Literal("--input"),
            InputRef(inputId),
            Literal("--output"),
            OutputRef(outputId)
        )

        val task = CommandTaskDefinition(
            id = id,
            name = "full-command",
            description = "A complete command task",
            executable = "python",
            args = args
        )

        assertEquals(id, task.id)
        assertEquals("full-command", task.name)
        assertEquals("A complete command task", task.description)
        assertEquals("python", task.executable)
        assertEquals(args, task.args)
    }

    @Test
    fun `implements TaskDefinition interface correctly`()
    {
        val task = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "interface-test",
            executable = "test"
        )

        // Should be usable as TaskDefinition
        val taskDef: TaskDefinition = task
        assertNotNull(taskDef.id)
        assertEquals("interface-test", taskDef.name)
        assertEquals(null, taskDef.description)
    }

    @Test
    fun `rejects blank name`()
    {
        assertFailsWith<IllegalArgumentException>("CommandTaskDefinition name must not be blank") {
            CommandTaskDefinition(
                id = UUID.randomUUID(),
                name = "",
                executable = "python"
            )
        }

        assertFailsWith<IllegalArgumentException>("CommandTaskDefinition name must not be blank") {
            CommandTaskDefinition(
                id = UUID.randomUUID(),
                name = "   ",
                executable = "python"
            )
        }
    }

    @Test
    fun `rejects blank executable`()
    {
        assertFailsWith<IllegalArgumentException>("CommandTaskDefinition executable must not be blank") {
            CommandTaskDefinition(
                id = UUID.randomUUID(),
                name = "test-task",
                executable = ""
            )
        }

        assertFailsWith<IllegalArgumentException>("CommandTaskDefinition executable must not be blank") {
            CommandTaskDefinition(
                id = UUID.randomUUID(),
                name = "test-task",
                executable = "  "
            )
        }
    }

    @Test
    fun `serialization round-trip preserves all fields`()
    {
        val original = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "serialize-test",
            description = "Test serialization",
            executable = "python",
            args = listOf(
                Literal("--input"),
                InputRef(UUID.randomUUID()),
                ParamRef("batch-size")
            )
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<CommandTaskDefinition>(encoded)

        assertEquals(original, decoded)
        assertEquals(original.id, decoded.id)
        assertEquals(original.name, decoded.name)
        assertEquals(original.description, decoded.description)
        assertEquals(original.executable, decoded.executable)
        assertEquals(original.args, decoded.args)
    }

    @Test
    fun `serialization works with null description`()
    {
        val task = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "null-desc",
            executable = "test"
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<CommandTaskDefinition>(encoded)

        assertEquals(task, decoded)
        assertEquals(null, decoded.description)
    }

    @Test
    fun `serialization works with empty args list`()
    {
        val task = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "empty-args",
            executable = "test",
            args = emptyList()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(task)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<CommandTaskDefinition>(encoded)

        assertEquals(task, decoded)
        assertEquals(emptyList(), decoded.args)
    }

    @Test
    fun `polymorphic serialization as TaskDefinition works`()
    {
        val original: TaskDefinition = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "polymorphic-test",
            executable = "python"
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<TaskDefinition>(encoded)

        assertEquals(original, decoded)
        assertTrue(decoded is CommandTaskDefinition)
        assertEquals("python", decoded.executable)
    }

    @Test
    fun `SerialName annotation is present for polymorphic serialization`()
    {
        val task = CommandTaskDefinition(
            id = UUID.randomUUID(),
            name = "test",
            executable = "test"
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString<TaskDefinition>(task)
        assertTrue(encoded.contains("\"type\":\"CommandTaskDefinition\""))
    }
}
