package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.data.InMemoryData
import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.common.application.data.StepCount
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*

class DataRetrievalProcessTest {

    class MockStudyDataService(
        private val fakeData: CollectedDataSet
    ) : StudyDataService {
        override suspend fun getCollectedData(
            studyId: UUID,
            studyDeploymentIds: Set<UUID>?,
            deviceRoleNames: Set<String>?,
            fields: Set<String>?,
            from: Instant?,
            to: Instant?,
            offsetDays: Int?
        ): CollectedDataSet {
            return fakeData
        }
    }

    private fun createDummyDataSet(): CollectedDataSet {
        val point = CollectedDataPoint(
            streamId = dk.cachet.carp.data.application.DataStreamId(
                studyDeploymentId = UUID.randomUUID(),
                deviceRoleName = "phone",
                dataType =  NamespacedId("dk.cachet.carp", "step_count")
            ),
            data = StepCount(steps = 5000),
            timestamp = Clock.System.now()
        )
        return CollectedDataSet(listOf(point))
    }

    @Test
    fun testDataRetrievalAndRegistration() {
        val registry = DataRegistry()
        val fakeDataSet = createDummyDataSet()
        val studyId = UUID.randomUUID()

        val mockService = MockStudyDataService(fakeDataSet)

        val retrievalProcess = DataRetrievalProcess(
            name = "load_stepcount",
            description = "Loads step count data",
            studyId = studyId
        ).apply {
            studyDataService = mockService
            dataRegistry = registry
        }

        val result = retrievalProcess.process(CollectedDataSet(emptyList()))
        println(result)

        assertNotNull(result, "Result should not be null.")
        assertEquals(1, result.points.size, "Should return the mocked dataset.")
    }
}
