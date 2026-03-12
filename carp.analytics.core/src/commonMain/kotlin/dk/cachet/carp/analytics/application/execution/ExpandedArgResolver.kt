package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID

/**
 * Resolves [dk.cachet.carp.analytics.application.plan.ExpandedArg] values to human-readable strings.
 *
 * Used during execution to convert abstract argument references into concrete paths
 * or values for logging and command-line rendering.
 */
interface ExpandedArgResolver
{
    /**
     * Resolve a DataReference to its actual path.
     *
     * @return The resolved path (e.g., "/workspace/step1/outputs/data.csv")
     *         or null if not found.
     */
    fun resolveDataRefPath( dataRefId: UUID ): String?

    /**
     * Get environment variable value.
     *
     * @return The environment variable value or null if not set.
     */
    fun getEnvVar( name: String ): String?
}

/**
 * Null resolver: returns null for everything.
 * Use when resolution context is not available.
 */
object NoOpResolver : ExpandedArgResolver
{
    override fun resolveDataRefPath( dataRefId: UUID ): String? = null
    override fun getEnvVar( name: String ): String? = null
}
