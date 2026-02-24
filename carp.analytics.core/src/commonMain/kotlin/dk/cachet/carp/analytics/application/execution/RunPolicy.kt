package dk.cachet.carp.analytics.application.execution

/**
 * Base contract for mergeable run policies.
 * Policies are immutable and contain no IO/runtime handles/paths.
 */
interface RunPolicy
{
    /**
     * Merge `override` onto this policy, where non-null values in `override` win.
     */
    val timeoutMs: Long?
}
