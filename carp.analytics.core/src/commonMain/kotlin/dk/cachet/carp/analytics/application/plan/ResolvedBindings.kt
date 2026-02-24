package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Fully resolved bindings for a planned step.
 *
 * Executors consume this structure and must not depend on DataRegistry directly.
 */
@Serializable
data class ResolvedBindings(
    val inputs: Map<UUID, DataRef> = emptyMap(),
    val outputs: Map<UUID, DataRef> = emptyMap(),
)
{
    fun input( id: UUID ): DataRef? = inputs[id]
    fun output( id: UUID ): DataRef? = outputs[id]
}

/**
 * Concrete reference to readable/writable data.
 * The [id] is typically a stable registry id; [type] is a semantic/format hint.
 */
@Serializable
data class DataRef(
    val id: UUID,
    val type: String,
)
{
    init
    {
        require(type.isNotBlank())
        {
            "DataRef.type must not be blank."
        }
    }
}
