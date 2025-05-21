package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*
import java.sql.Connection
import java.sql.DriverManager

class SQLiteWorkflowRepositoryTest {

    private lateinit var repo: SQLiteWorkflowRepository
    private lateinit var schemaConnection: Connection
    private val dbUrl = "jdbc:sqlite:file:repo-test?mode=memory&cache=shared"

    @BeforeTest
    fun setup() {
        schemaConnection = DriverManager.getConnection(dbUrl)
        schemaConnection.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE workflow_metadata (
                    id TEXT PRIMARY KEY,
                    study_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    version_major INTEGER NOT NULL,
                    version_minor INTEGER,
                    file_path TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """.trimIndent())
        }
        repo = SQLiteWorkflowRepository(dbUrl)
    }

    @AfterTest
    fun cleanup() {
        schemaConnection.close()
    }

    @Test
    fun `inserting duplicate id should fail`() = runTest {
        val workflow = exampleMetadata()
        val inserted1 = repo.create(workflow)
        val inserted2 = runCatching { repo.create(workflow) }.getOrNull()

        assertTrue(inserted1)
        assertNull(inserted2, "Second insert with same ID should fail or return null")
    }

    @Test
    fun `update nonexistent workflow returns false`() = runTest {
        val nonexistent = exampleMetadata()
        val updated = repo.update(nonexistent)
        assertFalse(updated)
    }

    @Test
    fun `second delete returns false`() = runTest {
        val workflow = exampleMetadata()
        repo.create(workflow)

        val deleted1 = repo.delete(workflow.studyId, workflow.id)
        val deleted2 = repo.delete(workflow.studyId, workflow.id)

        assertTrue(deleted1)
        assertFalse(deleted2)
    }

    @Test
    fun `listing from empty database returns empty list`() = runTest {
        val results = repo.listByStudyId(UUID.randomUUID())
        assertTrue(results.isEmpty())
    }

    private fun exampleMetadata(
        id: UUID = UUID.randomUUID(),
        studyId: UUID = UUID.randomUUID()
    ): StoredWorkflowMetadata {
        return StoredWorkflowMetadata(
            id = id,
            studyId = studyId,
            name = "Test",
            description = "Example",
            versionMajor = 1,
            versionMinor = 0,
            filePath = "/tmp/example.yaml",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
}
