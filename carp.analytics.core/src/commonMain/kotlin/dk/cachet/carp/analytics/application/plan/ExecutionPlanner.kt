package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * Compiles a workflow definition into an executable plan.
 *
 */
interface ExecutionPlanner
{
    fun plan( workflow: Workflow ): ExecutionPlan
}
