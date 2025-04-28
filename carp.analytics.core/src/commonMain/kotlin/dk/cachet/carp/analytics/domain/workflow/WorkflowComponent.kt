package dk.cachet.carp.analytics.domain.workflow

/**
 * Base interface for all workflow components.
 */
interface WorkflowComponent {
    val name: String
    val description: String?
}
