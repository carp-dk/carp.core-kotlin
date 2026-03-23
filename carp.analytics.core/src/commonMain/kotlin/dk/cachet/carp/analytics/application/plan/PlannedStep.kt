package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A single executable step inside an [ExecutionPlan], with all runtime-relevant information resolved.
 */
@Serializable
data class PlannedStep(
    val metadata: StepMetadata,
    val process: TasksRun,
    val bindings: ResolvedBindings,
    val environmentRef: UUID?
)
{
    init
    {
        require(metadata.name.isNotBlank() )
        {
            "PlannedStep.name must not be blank."
        }
    }
}
