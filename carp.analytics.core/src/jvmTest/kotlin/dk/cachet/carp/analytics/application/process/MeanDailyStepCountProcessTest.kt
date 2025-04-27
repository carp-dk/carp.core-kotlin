package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.StepCount
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class MeanDailyStepCountProcessTest {

    private fun createStepCountDataPoint(
        studyDeploymentId: UUID,
        deviceRoleName: String,
        timestamp: Instant,
        steps: Int
    ): CollectedDataPoint {
        return CollectedDataPoint(
            streamId = dk.cachet.carp.data.application.DataStreamId(
                studyDeploymentId = studyDeploymentId,
                deviceRoleName = deviceRoleName,
                dataType =  NamespacedId("dk.cachet.carp", "step_count")
            ),
            data = StepCount(steps),
            timestamp = timestamp
        )
    }

    @Test
    fun testMeanStepCountCalculation() {
        val process = MeanDailyStepCountProcess()

        val studyDeploymentId = UUID.randomUUID()
        val deviceRole = "phone"

        val now = Clock.System.now()


        val inputPoints = listOf(
            createStepCountDataPoint(studyDeploymentId, deviceRole, now, 4000),
            createStepCountDataPoint(studyDeploymentId, deviceRole, now.plus(1.hours), 6000),
            createStepCountDataPoint(studyDeploymentId, deviceRole, now.plus(2.hours), 8000)
        )

        val inputDataSet = CollectedDataSet(inputPoints)

        val output = process.process(inputDataSet)

        assertNotNull(output, "Output should not be null.")

        val points = output.points
        assertEquals(1, points.size, "Should produce one mean step count data point.")

        val meanStepCount = points.first().data as StepCount
        assertEquals(6000, meanStepCount.steps, "Mean step count should be (4000+6000+8000)/3 = 6000.")
    }

    @Test
    fun testEmptyInputReturnsNull() {
        val process = MeanDailyStepCountProcess()
        val emptyInput = CollectedDataSet(emptyList())

        val output = process.process(emptyInput)

        assertNull(output, "Output should be null for empty input.")
    }

    @Test
    fun testNoStepCountPointsReturnsNull() {
        val process = MeanDailyStepCountProcess()

        val studyDeploymentId = UUID.randomUUID()
        val now = Clock.System.now()

        // Create fake non-StepCount points by manually faking (pretending)
        val fakePoints = listOf(
            CollectedDataPoint(
                streamId = dk.cachet.carp.data.application.DataStreamId(
                    studyDeploymentId = studyDeploymentId,
                    deviceRoleName = "phone",
                    dataType = NamespacedId("dk.cachet.carp", "heart_rate")
                ),
                data = dk.cachet.carp.common.application.data.HeartRate(bpm = 70),
                timestamp = now
            )
        )

        val inputDataSet = CollectedDataSet(fakePoints)

        val output = process.process(inputDataSet)

        assertNull(output, "Output should be null when no StepCount points are present.")
    }
}
