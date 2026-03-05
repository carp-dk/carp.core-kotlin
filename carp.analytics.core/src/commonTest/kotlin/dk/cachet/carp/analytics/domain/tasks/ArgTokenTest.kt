package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for ArgToken implementations.
 *
 * Tests all ArgToken types for validation, serialization, and behavior.
 */
class ArgTokenTest
{

    @Test
    fun `Literal creates valid instances`()
    {
        val arg = Literal("--input")
        assertEquals("--input", arg.value)
    }

    @Test
    fun `Literal accepts blank values`()
    {
        // According to spec, no validation is required
        val arg = Literal("")
        assertEquals("", arg.value)
    }

    @Test
    fun `InputRef creates valid instances`()
    {
        val uuid = UUID.randomUUID()
        val arg = InputRef(uuid)
        assertEquals(uuid, arg.inputId)
    }

    @Test
    fun `OutputRef creates valid instances`()
    {
        val uuid = UUID.randomUUID()
        val arg = OutputRef(uuid)
        assertEquals(uuid, arg.outputId)
    }

    @Test
    fun `ParamRef creates valid instances`()
    {
        val arg = ParamRef("batch-size")
        assertEquals("batch-size", arg.name)
    }

    @Test
    fun `ArgToken serialization round-trip preserves all types`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val tokens: List<ArgToken> = listOf(
            Literal("--input"),
            InputRef(inputId),
            OutputRef(outputId),
            ParamRef("batch-size")
        )

        tokens.forEach { original ->
            val encoded = CoreAnalyticsSerializer.json.encodeToString(original)
            val decoded = CoreAnalyticsSerializer.json.decodeFromString<ArgToken>(encoded)
            assertEquals(original, decoded)
        }
    }

    @Test
    fun `ArgToken polymorphic serialization works correctly`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val tokens: List<ArgToken> = listOf(
            Literal("python"),
            InputRef(inputId),
            OutputRef(outputId)
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString(tokens)
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<List<ArgToken>>(encoded)

        assertEquals(tokens.size, decoded.size)
        assertEquals(tokens, decoded)
    }

    @Test
    fun `ArgToken types have correct SerialName annotations`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val literal = Literal("test")
        val input = InputRef(inputId)
        val output = OutputRef(outputId)
        val param = ParamRef("test")

        // Verify serialization includes correct type discriminators
        val literalJson = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(literal)
        val inputJson = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(input)
        val outputJson = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(output)
        val paramJson = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(param)

        assertTrue(literalJson.contains("\"type\":\"Literal\""))
        assertTrue(inputJson.contains("\"type\":\"InputRef\""))
        assertTrue(outputJson.contains("\"type\":\"OutputRef\""))
        assertTrue(paramJson.contains("\"type\":\"ParamRef\""))
    }

    @Test
    fun `Literal handles special characters and empty strings`()
    {
        val specialChars = Literal("--flag=\"value with spaces\"")
        val empty = Literal("")
        val whitespace = Literal("   ")

        assertEquals("--flag=\"value with spaces\"", specialChars.value)
        assertEquals("", empty.value)
        assertEquals("   ", whitespace.value)

        // All should serialize/deserialize correctly
        val encodedSpecial = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(specialChars)
        val decodedSpecial = CoreAnalyticsSerializer.json.decodeFromString<ArgToken>(encodedSpecial)
        assertEquals(specialChars, decodedSpecial)
    }

    @Test
    fun `InputRef and OutputRef work with same UUID`()
    {
        val uuid = UUID.randomUUID()
        val input = InputRef(uuid)
        val output = OutputRef(uuid)

        assertEquals(uuid, input.inputId)
        assertEquals(uuid, output.outputId)

        // They should be different types even with same UUID
        assertFalse(input == output)
        assertEquals("InputRef", input::class.simpleName)
        assertEquals("OutputRef", output::class.simpleName)
    }

    @Test
    fun `ParamRef works with various parameter names`()
    {
        val simple = ParamRef("param")
        val dashed = ParamRef("param-name")
        val underscored = ParamRef("param_name")
        val dotted = ParamRef("param.name")

        assertEquals("param", simple.name)
        assertEquals("param-name", dashed.name)
        assertEquals("param_name", underscored.name)
        assertEquals("param.name", dotted.name)

        // All should serialize correctly
        listOf(simple, dashed, underscored, dotted).forEach { param ->
            val encoded = CoreAnalyticsSerializer.json.encodeToString<ArgToken>(param)
            val decoded = CoreAnalyticsSerializer.json.decodeFromString<ArgToken>(encoded)
            assertEquals(param, decoded)
        }
    }

    @Test
    fun `sealed interface prevents external implementations`()
    {
        // This is a compile-time test - we can't create new implementations
        // outside the defined variants, but we can test the sealed behavior
        val tokens: List<ArgToken> = listOf(
            Literal("test"),
            InputRef(UUID.randomUUID()),
            OutputRef(UUID.randomUUID()),
            ParamRef("test")
        )

        // All should be one of the known types
        tokens.forEach { token ->
            assertTrue(
                token is Literal || token is InputRef || token is OutputRef || token is ParamRef,
                "Token should be one of the defined variants"
            )
        }
    }
}
