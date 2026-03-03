package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.data.DataSchema
import dk.cachet.carp.analytics.domain.data.DataSource
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A structured, bindable output produced by a step execution.
 * A structured, bindable output produced by a step execution.
 */
@Serializable
data class OutputRef(
    val outputId: UUID,
    val source: DataSource,
    val format: FileFormat,
    val schema: DataSchema,
)
