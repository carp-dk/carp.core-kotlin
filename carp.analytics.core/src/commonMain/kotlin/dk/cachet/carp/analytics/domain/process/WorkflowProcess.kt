package dk.cachet.carp.analytics.domain.process

/**
 * Base trait for all processes that can be executed in a [dk.cachet.carp.analytics.domain.workflow.Workflow] [dk.cachet.carp.analytics.domain.workflow.Step].
 */
interface WorkflowProcess {
    val name: String
    val description: String
}
