package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Instant
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class StudyDataServiceDecoratorTest {

    private val studyId = UUID.randomUUID()
    private val now = Instant.parse("2025-04-27T00:00:00Z")

    private class TestStudyDataService : StudyDataService {
        var calledWith: StudyDataServiceRequest.GetCollectedData? = null

        override suspend fun getCollectedData(
            studyId: UUID,
            studyDeploymentIds: Set<UUID>?,
            deviceRoleName: Set<String>?,
            fields: Set<String>?,
            from: Instant?,
            to: Instant?,
            offsetDays: Int?
        ): CollectedDataSet {
            calledWith = StudyDataServiceRequest.GetCollectedData(
                studyId,
                studyDeploymentIds,
                deviceRoleName,
                fields,
                from,
                to,
                offsetDays
            )
            return CollectedDataSet() // Return empty for now
        }
    }

    @Test
    fun testGetCollectedDataIsDelegated() = runTest {
        val backend = TestStudyDataService()
        val decorator = StudyDataServiceDecorator(backend)

        val result = decorator.getCollectedData(
            studyId,
            fields = setOf("step_count"),
            from = now
        )

        assertNotNull(backend.calledWith)
        assertEquals(studyId, backend.calledWith!!.studyId)
        assertEquals(setOf("step_count"), backend.calledWith!!.fields)
        assertEquals(now, backend.calledWith!!.from)
        assertTrue(result.points.isEmpty())
    }
}
