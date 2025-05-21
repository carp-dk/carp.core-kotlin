package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.application.WorkflowStorageService
import dk.cachet.carp.analytics.domain.workflow.*
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.test.*
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class DBBackedWorkflowServiceTest {

    private lateinit var repo: SQLiteWorkflowRepository
    private lateinit var storage: WorkflowStorageService
    private lateinit var service: DBBackedWorkflowService
    private lateinit var tempDir: Path
    private lateinit var connection: Connection

    @BeforeTest
    fun setup() {
        tempDir = createTempDirectory("workflow_test_")
        storage = FileSystemWorkflowStorageService(tempDir.toFile())

        // Use named in-memory database with shared cache
        val dbUrl = "jdbc:sqlite:file:memdb1?mode=memory&cache=shared"
        connection = DriverManager.getConnection(dbUrl)

        initializeSchema(connection)
        repo = SQLiteWorkflowRepository(dbUrl)
        service = DBBackedWorkflowService(repo, storage)
    }

    @AfterTest
    fun cleanup() {
        connection.close()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `create and retrieve workflow successfully`() = runTest {
        val studyId = UUID.randomUUID()
        val workflow = createExampleWorkflow()

        val created = service.createWorkflow(studyId, workflow)
        assertTrue(created)

        val retrieved = service.getWorkflow(studyId, workflow.metadata.id)
        assertNotNull(retrieved)
        assertEquals(workflow.metadata.name, retrieved.metadata.name)
    }

    @Test
    fun `update workflow updates file path and metadata`() = runTest {
        val studyId = UUID.randomUUID()
        val original = createExampleWorkflow()
        val updated = original.copy(metadata = original.metadata.copy(description = "Updated description"))

        service.createWorkflow(studyId, original)
        val updatedSuccess = service.updateWorkflow(studyId, updated.metadata, updated)

        assertTrue(updatedSuccess)
        val result = service.getWorkflow(studyId, updated.metadata.id)
        assertEquals("Updated description", result?.metadata?.description)
    }

    @Test
    fun `listWorkflows returns all workflows for study`() = runTest {
        val studyId = UUID.randomUUID()
        service.createWorkflow(studyId, createExampleWorkflow("A"))
        service.createWorkflow(studyId, createExampleWorkflow("B"))

        val list = service.listWorkflows(studyId)
        assertEquals(2, list.size)
    }

    @Test
    fun `deleteWorkflow removes workflow`() = runTest {
        val studyId = UUID.randomUUID()
        val workflow = createExampleWorkflow()

        service.createWorkflow(studyId, workflow)
        val deleted = service.deleteWorkflow(studyId, workflow.metadata.id)

        assertTrue(deleted)
        assertNull(service.getWorkflow(studyId, workflow.metadata.id))
    }

    // Helpers

    private fun createExampleWorkflow(name: String = "Test Workflow"): Workflow {
        return Workflow(
            metadata = WorkflowMetadata(
                id = UUID.randomUUID(),
                name = name,
                description = "Test description",
                version = Version(1)
            )
        )
    }

    private fun initializeSchema(conn: Connection) {
        conn.createStatement().use { stmt ->
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
    }
}
