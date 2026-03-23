package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.InputDataSpec
import kotlinx.serialization.Serializable

/**
 * Resolved input binding carrying both original specification and resolved source.
 *
 * @property spec The original input specification with all metadata
 * @property resolvedSource The resolved data source with materialized paths
 */
@Serializable
data class ResolvedInput(
    val spec: InputDataSpec,
    val resolvedSource: ResolvedDataSource
)
{
    init
    {
        require( spec.name.isNotBlank() ) { "Input spec name must not be blank" }
    }
}