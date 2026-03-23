package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileDestination
import dk.cachet.carp.analytics.domain.data.RegistryDestination
import dk.cachet.carp.analytics.domain.data.DatabaseDestination
import dk.cachet.carp.analytics.domain.data.ApiDestination
import dk.cachet.carp.analytics.domain.data.DataDestination
import dk.cachet.carp.analytics.domain.data.StreamDestination
import kotlinx.serialization.Serializable

/**
 * Data destination with resolved/materialized paths and configuration.
 * Carries original destination spec plus resolved/computed values.
 */
@Serializable
sealed interface ResolvedDataDestination
{
    /** The original destination specification */
    val original: DataDestination

    @Serializable
    data class File(
        override val original: FileDestination,
        val resolvedPath: String  // Resolved to workspace paths
    ) : ResolvedDataDestination

    @Serializable
    data class Registry(
        override val original: RegistryDestination
    ) : ResolvedDataDestination

    @Serializable
    data class Database(
        override val original: DatabaseDestination
    ) : ResolvedDataDestination

    @Serializable
    data class Api(
        override val original: ApiDestination
    ) : ResolvedDataDestination

    @Serializable
    data class Stream(
        override val original: StreamDestination
    ) : ResolvedDataDestination
}