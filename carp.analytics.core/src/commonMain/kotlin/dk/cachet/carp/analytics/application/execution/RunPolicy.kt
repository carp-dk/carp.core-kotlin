package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

/**
 * Base contract for run policies.
 * Policies are immutable value objects and must not hold IO handles or filesystem paths.
 */
interface RunPolicy
{
    /** Abort remaining steps after the first step failure. */
    val stopOnFailure: Boolean

    /** Overall wall-clock timeout for the entire run, in milliseconds. Null = no limit. */
    val timeoutMs: Long?

    /** Treat plan-level warnings as hard failures. */
    val failOnWarnings: Boolean

    /** Maximum number of retry attempts per step. 0 = no retries. */
    val maxAttempts: Int
}

/**
 * Sensible default policy: stop on first failure, no timeout, no retries.
 */
@Serializable
data class DefaultRunPolicy(
    override val stopOnFailure: Boolean = true,
    override val timeoutMs: Long? = null,
    override val failOnWarnings: Boolean = false,
    override val maxAttempts: Int = 0
) : RunPolicy
