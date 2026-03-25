package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.plan.ExpandedArg

/**
 * Display [ExpandedArg] in compact form (UUID only).
 *
 * Safe to call anywhere; doesn't require external context.
 * Returns UUID string for references, template for path substitutions, env var for env references.
 *
 * Example output:
 *   Literal("script.py") → "script.py"
 *   DataReference(UUID(...)) → "550e8400-e29b-41d4-..."
 *   PathSubstitution(UUID(...), "--input=()") → "--input=550e8400-..."
 *   EnvironmentVariable("MODEL_PATH", "--model=()") → "--model=$(env.MODEL_PATH)"
 */
fun ExpandedArg.toCompactString(): String = when ( this )
{
    is ExpandedArg.Literal -> value
    is ExpandedArg.DataReference -> id.toString()
    is ExpandedArg.PathSubstitution -> template.replace( "()", id.toString() )
    is ExpandedArg.EnvironmentVariable -> template.replace( "()", "$(env.$name)")
}

/**
 * Display [ExpandedArg] with resolution context.
 *
 * Attempts to resolve to human-readable form (paths, values), falls back to compact form.
 *
 * @param resolver Context for resolving references. Can be [NoOpResolver] for no resolution.
 * @param mode Controls display format (COMPACT, RESOLVED, VERBOSE)
 *
 * Example outputs for DataReference(UUID(...)):
 * - COMPACT: "550e8400-e29b-41d4-..."
 * - RESOLVED: "/workspace/step1/outputs/data.csv"
 * - VERBOSE: "/workspace/step1/outputs/data.csv (550e8400-...)"
 *
 * Example outputs for PathSubstitution:
 * - COMPACT: "--input=550e8400-..."
 * - RESOLVED: "--input=/workspace/step1/outputs/data.csv"
 * - VERBOSE: "--input=/workspace/step1/outputs/data.csv [550e8400-...]"
 *
 * Example outputs for EnvironmentVariable:
 * - COMPACT: "--model=$(env.MODEL_PATH)"
 * - RESOLVED: "--model=/models/v2.pkl" (if env var set)
 * - VERBOSE: "--model=/models/v2.pkl (env: MODEL_PATH)"
 */
fun ExpandedArg.toResolvedString(
    resolver: ExpandedArgResolver = NoOpResolver,
    mode: ArgumentDisplayMode = ArgumentDisplayMode.RESOLVED
): String = when ( this )
{
    is ExpandedArg.Literal -> value

    is ExpandedArg.DataReference -> resolveDataReference( this, resolver, mode )

    is ExpandedArg.PathSubstitution -> resolvePathSubstitution( this, resolver, mode )

    is ExpandedArg.EnvironmentVariable -> resolveEnvironmentVariable( this, resolver, mode )
}

// Helper functions extracted to keep the public API function simple and under complexity threshold.
private fun resolveDataReference(
    arg: ExpandedArg.DataReference,
    resolver: ExpandedArgResolver,
    mode: ArgumentDisplayMode
): String
{
    val resolved = resolver.resolveDataRefPath( arg.id )
    return if ( resolved != null )
    {
        if ( mode == ArgumentDisplayMode.VERBOSE )
        {
            "$resolved (${arg.id})"
        }
        else
        {
            resolved
        }
    }
    else
    {
        arg.id.toString()
    }
}

private fun resolvePathSubstitution(
    arg: ExpandedArg.PathSubstitution,
    resolver: ExpandedArgResolver,
    mode: ArgumentDisplayMode
): String
{
    val resolved = resolver.resolveDataRefPath( arg.id )
    val value = resolved ?: arg.id.toString()
    val expanded = arg.template.replace( "()", value )
    return if ( mode == ArgumentDisplayMode.VERBOSE )
    {
        "$expanded [${arg.id}]"
    }
    else
    {
        expanded
    }
}

private fun resolveEnvironmentVariable(
    arg: ExpandedArg.EnvironmentVariable,
    resolver: ExpandedArgResolver,
    mode: ArgumentDisplayMode
): String
{
    val envValue = resolver.getEnvVar( arg.name )
    val value = envValue ?: "$(env.${arg.name})"
    val expanded = arg.template.replace( "()", value )
    return if ( mode == ArgumentDisplayMode.VERBOSE )
    {
        "$expanded (env: ${arg.name})"
    }
    else
    {
        expanded
    }
}

/**
 * Display all args in a list with resolution context.
 *
 * Convenience for rendering entire command lines.
 */
fun List<ExpandedArg>.toResolvedStrings(
    resolver: ExpandedArgResolver = NoOpResolver,
    mode: ArgumentDisplayMode = ArgumentDisplayMode.RESOLVED
): List<String> = map { it.toResolvedString( resolver, mode ) }
