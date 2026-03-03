package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.plan.ExecutionPlan
import dk.cachet.carp.common.application.UUID

/**
 * Drives execution of an [ExecutionPlan] and returns a full [ExecutionReport].
 *
 * Implementations are responsible for step orchestration, workspace preparation,
 * and result collection. They must not contain workflow planning logic.
 */
interface PlanExecutor
{
    /**
     * Execute all steps in [plan] under a given [runId] and [policy].
     *
     * @param plan     The compiled, runnable execution plan.
     * @param runId    Unique identifier for this run.
     * @param policy   Controls stop-on-failure, timeouts, and retry behaviour.
     * @return A completed [ExecutionReport] covering all planned steps.
     */
    fun execute(
        plan: ExecutionPlan,
        runId: UUID,
        policy: RunPolicy = DefaultRunPolicy()
    ): ExecutionReport
}

