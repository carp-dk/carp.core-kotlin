package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataQuery
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.*

class StudyDataServiceDecoratorTest
{

    private val studyId = UUID.randomUUID()
    private val now = Instant.parse("2025-04-27T00:00:00Z")

    private class TestStudyDataService : StudyDataService
    {
        var calledWith: StudyDataServiceRequest.GetCollectedData? = null

        override suspend fun getCollectedData(
            studyId: UUID,
            query: CollectedDataQuery
        ): CollectedDataSet
        {
            calledWith = StudyDataServiceRequest.GetCollectedData(
                studyId, query
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
            CollectedDataQuery(
                fields = setOf("step_count"),
                from = now
            )
        )

        assertNotNull(backend.calledWith)
        assertEquals(studyId, backend.calledWith!!.studyId)
        assertEquals(setOf("step_count"), backend.calledWith!!.query.fields)
        assertEquals(now, backend.calledWith!!.query.from)
        assertTrue(result.points.isEmpty())
    }
}
