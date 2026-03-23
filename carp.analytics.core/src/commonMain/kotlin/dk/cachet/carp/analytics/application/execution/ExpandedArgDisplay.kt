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

    is ExpandedArg.DataReference ->
    {
        val resolved = resolver.resolveDataRefPath( id )
        when
        {
            resolved != null && mode == ArgumentDisplayMode.VERBOSE ->
                "$resolved ($id)"
            resolved != null ->
                resolved
            else ->
                id.toString()
        }
    }

    is ExpandedArg.PathSubstitution ->
    {
        val resolved = resolver.resolveDataRefPath( id )
        val value = resolved ?: id.toString()
        val expanded = template.replace( "()", value )
        when ( mode )
        {
            ArgumentDisplayMode.VERBOSE -> "$expanded [$id]"
            else -> expanded
        }
    }

    is ExpandedArg.EnvironmentVariable ->
    {
        val envValue = resolver.getEnvVar( name )
        val value = envValue ?: "$(env.$name)"
        val expanded = template.replace( "()", value )
        when ( mode )
        {
            ArgumentDisplayMode.VERBOSE -> "$expanded (env: $name)"
            else -> expanded
        }
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

/**
 * Create a command line string from expanded arguments.
 *
 * Joins all arguments with spaces, suitable for logging or display.
 */
fun List<ExpandedArg>.toResolvedCommandLine(
    resolver: ExpandedArgResolver = NoOpResolver,
    mode: ArgumentDisplayMode = ArgumentDisplayMode.RESOLVED
): String = toResolvedStrings( resolver, mode ).joinToString( " " )