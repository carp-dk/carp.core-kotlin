package dk.cachet.carp.analytics.application.plan

/**
 * Interface for computing hashes of execution plans.
 * Dependency Injection Pattern:
 * - Core defines the interface
 * - DSP provides the implementation
 * - Executor passes hasher to plan.diagnostics()
 */
interface PlanHasher
{
    /**
     * Computes a hash of the execution plan.
     *
     * @param plan The ExecutionPlan to hash
     * @return Hex-encoded hash string (stable and deterministic)
     */
    fun hash( plan: ExecutionPlan ): String
}
