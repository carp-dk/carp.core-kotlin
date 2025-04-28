package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.common.application.data.StepCount
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Process that calculates the mean daily step count per study deployment.
 */
@Serializable
@SerialName("mean_daily_step_count")
class MeanDailyStepCountProcess : AnalysisProcess {

    override val name: String = "mean_daily_step_count"
    override val description: String = "Calculate mean daily step count per participant."

    override fun process(input: CollectedDataSet): CollectedDataSet? {
        if (input.points.isEmpty()) {
            println("No data points provided.")
            return null
        }

        // Step 1: Filter StepCount data points
        val stepCountPoints = input.points.filter { it.data is StepCount }

        if (stepCountPoints.isEmpty()) {
            println("No StepCount data points found in input.")
            return null
        }

        // Step 2: Group by StudyDeploymentId and day
        val grouped = stepCountPoints.groupBy { point ->
            val deploymentId = point.streamId.studyDeploymentId
            val day = point.timestamp.toLocalDateTime(TimeZone.UTC).date
            Pair(deploymentId, day)
        }

        // Step 3: Calculate mean steps per day for each group
        val meanStepPoints = grouped.map { (key, points) ->
            val (deploymentId, day) = key

            val steps = points.map { (it.data as StepCount).steps }
            val meanSteps = steps.average().toInt()

            createMeanStepCountDataPoint(deploymentId, day, meanSteps)
        }

        return CollectedDataSet(meanStepPoints)
    }

    private fun createMeanStepCountDataPoint(
        deploymentId: UUID,
        day: LocalDate,
        meanSteps: Int
    ): CollectedDataPoint {
        val stepCount = StepCount(meanSteps)

        // Fake timestamp based on day
        val timestamp = day.atStartOfDayIn(TimeZone.UTC)

        return CollectedDataPoint(
            streamId = dk.cachet.carp.data.application.DataStreamId(
                studyDeploymentId = deploymentId,
                deviceRoleName = "aggregated", // using the device role name "aggregated" to indicate this is an aggregated rather than the original data
                dataType = NamespacedId("dk.cachet.carp", "step_count")
            ),
            data = stepCount, // TODO: make new data type for mean step count or dynamic data type for statistics
            timestamp = timestamp
        )
    }
}
