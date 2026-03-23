package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import kotlinx.serialization.Serializable

/**
 * Resolved output binding carrying both original specification and resolved destination.
 *
 * @property spec The original output specification with all metadata
 * @property resolvedDestination The resolved data destination with materialized paths
 */
@Serializable
data class ResolvedOutput(
    val spec: OutputDataSpec,
    val resolvedDestination: ResolvedDataDestination
)
{
    init
    {
        require( spec.name.isNotBlank() ) { "Output spec name must not be blank" }
    }
}