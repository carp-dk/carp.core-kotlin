package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.UrlSource
import dk.cachet.carp.analytics.domain.data.DatabaseSource
import dk.cachet.carp.analytics.domain.data.InMemorySource
import dk.cachet.carp.analytics.domain.data.ApiSource
import dk.cachet.carp.analytics.domain.data.DataSource
import dk.cachet.carp.analytics.domain.data.StepOutputSource
import dk.cachet.carp.analytics.domain.data.StreamSource
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Data source with resolved/materialized paths and configuration.
 * Carries original source spec plus resolved/computed values.
 */
@Serializable
sealed interface ResolvedDataSource
{
    /** The original source specification */
    val original: DataSource

    @Serializable
    data class FileSystem(
        override val original: FileSystemSource,
        val resolvedPath: String  // Resolved to workspace paths
    ) : ResolvedDataSource

    @Serializable
    data class Url(
        override val original: UrlSource
    ) : ResolvedDataSource

    @Serializable
    data class Database(
        override val original: DatabaseSource
    ) : ResolvedDataSource

    @Serializable
    data class InMemory(
        override val original: InMemorySource
    ) : ResolvedDataSource

    @Serializable
    data class Api(
        override val original: ApiSource
    ) : ResolvedDataSource

    @Serializable
    data class Stream(
        override val original: StreamSource
    ) : ResolvedDataSource

    @Serializable
    data class StepOutput(
        override val original: StepOutputSource,
        val producerStepId: UUID,   // Resolved to actual UUID
        val producerOutputId: UUID   // Resolved to actual UUID
    ) : ResolvedDataSource
}