package dk.cachet.carp.analytics.application.plan

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class CommandSpecTest
{

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = false
    }

    @Test
    fun `CommandRun round-trips with defaults`()
    {
        val run = CommandSpec(executable = "bash")

        val encoded = json.encodeToString(run)
        val decoded = json.decodeFromString<CommandSpec>(encoded)

        assertEquals(run, decoded)
        assertEquals(emptyList(), decoded.args)
    }

    @Test
    fun `CommandRun round-trips with populated fields`()
    {
        val run = CommandSpec(
            executable = "python",
            args = listOf("-c", "print('hi')").map { ExpandedArg.Literal(it) }
        )

        val encoded = json.encodeToString(run)
        val decoded = json.decodeFromString<CommandSpec>(encoded)

        assertEquals(run, decoded)
    }

    @Test
    fun `CommandRun validation covers blank executable cases`()
    {
        val emptyEx = assertFailsWith<IllegalArgumentException> { CommandSpec(executable = "") }
        assertEquals("CommandRun.executable must not be blank.", emptyEx.message)

        val emptyExArgs = assertFailsWith<IllegalArgumentException> {
            CommandSpec(
                executable = "",
                args = listOf("-c", "print('hi')").map { ExpandedArg.Literal(it) }
            )
        }
        assertEquals("CommandRun.executable must not be blank.", emptyExArgs.message)

        val blankEx = assertFailsWith<IllegalArgumentException> { CommandSpec(executable = " ") }
        assertEquals("CommandRun.executable must not be blank.", blankEx.message)
    }

    @Test
    fun `CommandRun equality and hashCode consider all fields`()
    {
        val base = CommandSpec(
            executable = "prog",
            args = listOf(ExpandedArg.Literal("--x")),
        )

        val diffExec = base.copy(executable = "other")
        val diffArgs = base.copy(args = listOf(ExpandedArg.Literal("--different")))

        assertEquals(base, base)

        assertNotEquals(base, diffExec)
        assertNotEquals(base, diffArgs)
    }


    @Test
    fun `CommandRun companion serializer is available`()
    {
        assertNotNull(CommandSpec.serializer())
    }
}
