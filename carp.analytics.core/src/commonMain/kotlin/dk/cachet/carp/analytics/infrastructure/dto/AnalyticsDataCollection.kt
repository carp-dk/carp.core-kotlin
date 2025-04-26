package dk.cachet.carp.analytics.infrastructure.dto

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.DataStreamId

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A simple wrapper around a list of [AnalyticsDataPoint]s.
 * Used to represent a collection of typed data for processing within a workflow step.
 */
@Serializable
@SerialName("dk.cachet.carp.analytics.AnalyticsDataCollection")
data class AnalyticsDataCollection(
    val points: List<AnalyticsDataPoint> = emptyList()
) {
    /**
     * Filter by stream ID.
     */
    fun filterByStream(streamId: DataStreamId): AnalyticsDataCollection =
        AnalyticsDataCollection(points.filter { it.streamId == streamId })

    /**
     * Filter by time range.
     */
    fun filterByTimeRange(start: Instant, end: Instant): AnalyticsDataCollection =
        AnalyticsDataCollection(points.filter { it.timestamp in start..end })

    /**
     * Extract data points of a specific [Data] type.
     */
    inline fun <reified T : Data> filterByType(): List<T> =
        points.mapNotNull { it.data as? T }
}
