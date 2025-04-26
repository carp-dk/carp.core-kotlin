package dk.cachet.carp.analytics.infrastructure.dto

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.DataStreamId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single typed data point from a CARP data stream, annotated with its stream origin and timestamp.
 */
@Serializable
@SerialName("dk.cachet.carp.analytics.AnalyticsDataPoint")
data class AnalyticsDataPoint(
    val streamId: DataStreamId,
    val timestamp: Instant,
    val data: Data // Polymorphic, must be registered in serializers module
)
