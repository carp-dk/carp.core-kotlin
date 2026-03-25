package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.plan.ExpandedArg
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpandedArgResolverTest
{

    @Test
    fun compactStringShowsUuidOnly()
    {
        val arg = ExpandedArg.DataReference(UUID("550e8400-e29b-41d4-a716-446655440000"))
        val result = arg.toCompactString()
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result)
    }

    @Test
    fun compactStringLiteral()
    {
        val arg = ExpandedArg.Literal("hello.txt")
        val result = arg.toCompactString()
        assertEquals("hello.txt", result)
    }

    @Test
    fun resolvedStringUsesResolver()
    {
        val arg = ExpandedArg.DataReference( UUID( "550e8400-e29b-41d4-a716-446655440000" ) )
        val resolver =
            object : ExpandedArgResolver
            {
                override fun resolveDataRefPath( dataRefId: UUID ) = "/workspace/data.csv"
                override fun getEnvVar( name: String ) = null
            }
        val result = arg.toResolvedString(resolver, ArgumentDisplayMode.RESOLVED)
        assertEquals("/workspace/data.csv", result)
    }

    @Test
    fun resolvedStringFallsBackToUuid()
    {
        val arg = ExpandedArg.DataReference(UUID("550e8400-e29b-41d4-a716-446655440000"))
        val result = arg.toResolvedString(NoOpResolver)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result)
    }

    @Test
    fun verboseIncludesBoth()
    {
        val arg = ExpandedArg.DataReference(UUID("550e8400-e29b-41d4-a716-446655440000"))
        val resolver =
            object : ExpandedArgResolver
            {
                override fun resolveDataRefPath( dataRefId: UUID ) = "/workspace/data.csv"
                override fun getEnvVar( name: String ) = null
            }
        val result = arg.toResolvedString(resolver, ArgumentDisplayMode.VERBOSE)
        assertTrue(result.contains("/workspace/data.csv"))
        assertTrue(result.contains("550e8400"))
    }

    @Test
    fun pathSubstitutionResolved()
    {
        val arg = ExpandedArg.PathSubstitution(
            template = "--input=$()",
            id = UUID("550e8400-e29b-41d4-a716-446655440000")
        )
        val resolver =
            object : ExpandedArgResolver
            {
                override fun resolveDataRefPath( dataRefId: UUID ) = "/workspace/data.csv"
                override fun getEnvVar( name: String ) = null
            }
        val result = arg.toResolvedString(resolver)
        assertEquals("--input=$/workspace/data.csv", result)
    }

    @Test
    fun listExtensionWorks()
    {
        val args = listOf(
            ExpandedArg.Literal("extract.py"),
            ExpandedArg.DataReference(UUID("550e8400-e29b-41d4-a716-446655440000"))
        )
        val resolver =
            object : ExpandedArgResolver
            {
                override fun resolveDataRefPath( dataRefId: UUID ) = "/workspace/data.csv"
                override fun getEnvVar( name: String ) = null
            }
        val results = args.toResolvedStrings(resolver)
        assertEquals(2, results.size)
        assertEquals("extract.py", results[0])
        assertEquals("/workspace/data.csv", results[1])
    }
}
