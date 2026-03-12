package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.plan.ExpandedArg

/**
 * Display [ExpandedArg] in compact form (UUID only).
 *
 * Safe to call anywhere; doesn't require external context.
 * Example output: "550e8400-e29b-41d4-..."
 */
fun ExpandedArg.toCompactString(): String = when (this)
{
    is ExpandedArg.Literal -> value
    is ExpandedArg.DataReference -> dataRefId.toString()
    is ExpandedArg.PathSubstitution -> template.replace("$()", dataRefId.toString())
}

/**
 * Display [ExpandedArg] with resolution context.
 *
 * Attempts to resolve to human-readable form (paths, values), falls back to UUID.
 *
 * @param resolver Context for resolving references. Can be [NoOpResolver] for no resolution.
 * @param mode Controls display format (COMPACT, RESOLVED, VERBOSE)
 *
 * Example outputs:
 * - COMPACT: "550e8400-e29b-41d4-..."
 * - RESOLVED: "/workspace/step1/outputs/data.csv"
 * - VERBOSE: "/workspace/step1/outputs/data.csv (550e8400-...)"
 */
fun ExpandedArg.toResolvedString(
    resolver: ExpandedArgResolver = NoOpResolver,
    mode: ArgumentDisplayMode = ArgumentDisplayMode.RESOLVED
): String = when (this)
{
    is ExpandedArg.Literal -> value

    is ExpandedArg.DataReference ->
    {
        val resolved = resolver.resolveDataRefPath(dataRefId)
        when
        {
            resolved != null && mode == ArgumentDisplayMode.VERBOSE ->
                "$resolved ($dataRefId)"
            resolved != null ->
                resolved
            else ->
                dataRefId.toString()
        }
    }

    is ExpandedArg.PathSubstitution ->
    {
        val resolved = resolver.resolveDataRefPath(dataRefId)
        val value = resolved ?: dataRefId.toString()
        val expanded = template.replace("$()", value)
        when (mode)
        {
            ArgumentDisplayMode.VERBOSE -> "$expanded [$dataRefId]"
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
): List<String> = map { it.toResolvedString(resolver, mode) }
