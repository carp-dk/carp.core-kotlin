package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Author-time container for a workflow definition and its required registries.
 *
 * This is the validation and compilation unit for Author → Plan.
 */
@Serializable
data class WorkflowDefinition(
    val workflow: Workflow,
    val environments: Map<UUID, EnvironmentDefinition> = emptyMap()
)

