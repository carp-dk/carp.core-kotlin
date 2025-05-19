package dk.cachet.carp.data.infrastructure.db

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

class SQLiteStudyDataRepositoryTest {

    private lateinit var repository: SQLiteStudyDataRepository

    @BeforeTest
    fun setup() {
        val dbPath = requireNotNull(this::class.java.getResource("/test.db"))
        val jdbcUrl = "jdbc:sqlite:$dbPath"
        repository = SQLiteStudyDataRepository(jdbcUrl)
    }

    @Test
    fun testFetchAllStepCountData() = runTest {
        val studyId = UUID("f8e5c3ef-8739-42ab-80e8-7be7c1ea4e61")
        val points = repository.queryData(studyId)

        assertTrue(points.isNotEmpty())
        assertTrue(points.all { it.streamId.dataType.name == "step_count" })
    }

    @Test
    fun testFilterBySubjectDeploymentId() = runTest {
        val studyId = UUID("f8e5c3ef-8739-42ab-80e8-7be7c1ea4e61")
        val subjectId = UUID("53d07e59-5c79-4e90-9277-6d12c1c6dca4")

        val points = repository.queryData(studyId, subjectDeploymentIds = setOf(subjectId))

        assertTrue(points.all { it.streamId.studyDeploymentId == subjectId })
    }

    @Test
    fun testFilterByDeviceRoleName() = runTest {
        val studyId = UUID("f8e5c3ef-8739-42ab-80e8-7be7c1ea4e61")

        val points = repository.queryData(
            studyId,
            deviceRoleNames = setOf("phone")
        )

        assertTrue(points.all { it.streamId.deviceRoleName == "phone" })
    }

    @Test
    fun testFilterByDataTypeName() = runTest {
        val studyId = UUID("f8e5c3ef-8739-42ab-80e8-7be7c1ea4e61")

        val points = repository.queryData(
            studyId,
            fields = setOf("step_count")
        )

        assertTrue(points.all { it.streamId.dataType.name == "step_count" })
    }

    @Test
    fun testFilterByTimeRange() = runTest {
        val studyId = UUID("f8e5c3ef-8739-42ab-80e8-7be7c1ea4e61")

        val from = Instant.fromEpochMilliseconds(1670002400000)
        val to = Instant.fromEpochMilliseconds(1670007200000)

        val points = repository.queryData(
            studyId,
            from = from,
            to = to
        )

        assertTrue(points.all { it.timestamp >= from && it.timestamp <= to })
    }
}
