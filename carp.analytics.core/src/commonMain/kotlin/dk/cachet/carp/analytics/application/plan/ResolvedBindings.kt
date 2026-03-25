package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Fully resolved bindings for a planned step.
 *
 * Contains both original input/output specifications and resolved data sources/destinations.
 * Executors consume this structure and have all necessary information for data handling.
 *
 * @property inputs Map from input ID to resolved input (spec + source)
 * @property outputs Map from output ID to resolved output (spec + destination)
 */
@Serializable
data class ResolvedBindings(
    val inputs: Map<UUID, ResolvedInput> = emptyMap(),
    val outputs: Map<UUID, ResolvedOutput> = emptyMap(),
)
{
    /**
     * Retrieves a resolved input binding by ID.
     *
     * @param id The input port ID
     * @return ResolvedInput if found, null otherwise
     */
    fun input( id: UUID ): ResolvedInput? = inputs[id]

    /**
     * Retrieves a resolved output binding by ID.
     *
     * @param id The output port ID
     * @return ResolvedOutput if found, null otherwise
     */
    fun output( id: UUID ): ResolvedOutput? = outputs[id]
}
