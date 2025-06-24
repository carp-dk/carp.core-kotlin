package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.data.Data

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A simple wrapper around a list of [CollectedDataPoint]s.
 * Used to represent a collection of typed data for processing within a workflow step.
 */
@Serializable
@SerialName("dk.cachet.carp.analytics.AnalyticsDataCollection")
data class CollectedDataSet(
    val points: List<CollectedDataPoint> = emptyList()
) {
    /**
     * Filter by stream ID.
     */
    fun filterByStream(streamId: DataStreamId): CollectedDataSet =
        CollectedDataSet(points.filter { it.streamId == streamId })

    /**
     * Filter by time range.
     */
    fun filterByTimeRange(start: Instant, end: Instant): CollectedDataSet =
        CollectedDataSet(points.filter { it.timestamp in start..end })

    /**
     * Extract data points of a specific [Data] type.
     */
    inline fun <reified T : Data> filterByType(): List<T> =
        points.mapNotNull { it.data as? T }
}
