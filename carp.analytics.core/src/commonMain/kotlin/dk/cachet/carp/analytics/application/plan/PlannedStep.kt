package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A single executable step inside an [ExecutionPlan], with all runtime-relevant information resolved.
 */
@Serializable
data class PlannedStep(
    val stepId: UUID,
    val name: String,
    val process: TasksRun,
    val bindings: ResolvedBindings,
    val environmentRef: UUID?
)
{
    init
    {
        require(name.isNotBlank())
        {
            "PlannedStep.name must not be blank."
        }
    }
}
