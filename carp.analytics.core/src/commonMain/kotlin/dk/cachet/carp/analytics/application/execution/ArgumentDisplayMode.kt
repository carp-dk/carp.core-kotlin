package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

/**
 * Controls how [dk.cachet.carp.analytics.application.plan.ExpandedArg] is displayed.
 */
@Serializable
enum class ArgumentDisplayMode
{
    /**
     * Compact mode: Show UUIDs only.
     * Example: "550e8400-e29b-41d4-..."
     */
    COMPACT,

    /**
     * Resolved mode: Show actual paths/values when available, UUID fallback.
     * Example: "/workspace/step1/outputs/data.csv"
     */
    RESOLVED,

    /**
     * Verbose mode: Show both path and UUID for debugging.
     * Example: "/workspace/step1/outputs/data.csv (550e8400-...)"
     */
    VERBOSE
}
