package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition

/**
 * Compiles a workflow definition into an executable plan.
 */
interface ExecutionPlanner
{
    fun plan( definition: WorkflowDefinition ): ExecutionPlan
}
