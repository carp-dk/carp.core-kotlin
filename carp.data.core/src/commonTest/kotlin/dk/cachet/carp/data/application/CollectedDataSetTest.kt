package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.StepCount
import dk.cachet.carp.data.infrastructure.dataStreamId
import kotlinx.datetime.Instant
import kotlin.test.*

class CollectedDataSetTest {

    private val streamId = dataStreamId<StepCount>(UUID.randomUUID(), "phone")
    private val now = Instant.parse("2025-04-27T12:00:00Z")
    private val later = Instant.parse("2025-04-27T13:00:00Z")

    private val point1 = CollectedDataPoint(streamId, now, StepCount(steps = 10))
    private val point2 = CollectedDataPoint(streamId, later, StepCount(steps = 20))

    @Test
    fun testFilterByType() {
        val set = CollectedDataSet(listOf(point1, point2))
        val steps = set.filterByType<StepCount>()

        assertEquals(2, steps.size)
        assertTrue(steps.all { it.steps > 0 })
    }

    @Test
    fun testFilterByStream() {
        val set = CollectedDataSet(listOf(point1, point2))
        val filtered = set.filterByStream(streamId)

        assertEquals(2, filtered.points.size)
    }

    @Test
    fun testFilterByTimeRange() {
        val set = CollectedDataSet(listOf(point1, point2))
        val filtered = set.filterByTimeRange(now, later)

        assertEquals(2, filtered.points.size)
    }
}
