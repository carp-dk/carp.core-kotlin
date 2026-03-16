package dk.cachet.carp.analytics.infrastructure.execution


/**
 * Controls behaviour of environment orchestrator.
 */
data class EnvironmentConfig(
    /**
     * Check registry before setup.
     * If true, reuse existing environment if found.
     */
    val reuseExisting: Boolean = true,

    /**
     * Clean-up policy applied at workflow end.
     * REUSE: Keep environments
     * CLEAN: Remove data but keep structure
     * PURGE: Delete entirely
     */
    val cleanupPolicy: CleanupPolicy = CleanupPolicy.REUSE,

    /**
     * When to set up environments.
     * EAGER: Before all steps
     * LAZY: Just-in-time per step
     */
    val setupTiming: SetupTiming = SetupTiming.EAGER,

    /**
     * How to handle setup failures.
     * FAIL_FAST: Fail immediately
     * CONTINUE: Log and continue
     * RETRY: Retry with configurable attempts
     */
    val errorHandling: ErrorHandling = ErrorHandling.FAIL_FAST,

    /**
     * Retry attempts (for ErrorHandling.RETRY).
     */
    val retryAttempts: Int = 3,

    /**
     * Retry backoff in milliseconds.
     */
    val retryBackoffMs: Long = 1000
)

enum class CleanupPolicy
{
    REUSE, // Keep environments
    CLEAN, // Remove data
    PURGE // Delete entirely
}

enum class SetupTiming
{
    EAGER, // Before all steps
    LAZY // Just-in-time per step
}

enum class ErrorHandling
{
    FAIL_FAST, // Fail immediately
    CONTINUE, // Log and continue
    RETRY // Retry
}
