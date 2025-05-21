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

class DBBackedWorkflowServiceFailureTest {

    private lateinit var service: DBBackedWorkflowService
    private lateinit var repo: SQLiteWorkflowRepository
    private lateinit var connection: Connection
    private lateinit var tempDir: Path
    private lateinit var storage: WorkflowStorageService

    private val dbUrl = "jdbc:sqlite:file:workflow-failure-test?mode=memory&cache=shared"

    @BeforeTest
    fun setup() {
        tempDir = createTempDirectory("workflow_failure_test_")
        storage = FileSystemWorkflowStorageService(tempDir.toFile())

        connection = DriverManager.getConnection(dbUrl)
        connection.createStatement().use { stmt ->
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
        service = DBBackedWorkflowService(repo, storage)
    }

    @AfterTest
    fun cleanup() {
        connection.close()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `getWorkflow returns null when file is missing`() = runTest {
        val studyId = UUID.randomUUID()
        val workflow = createExampleWorkflow()

        val filePath = tempDir.resolve("fake.yaml").toFile().absolutePath
        val metadata = StoredWorkflowMetadata.fromMetadata(workflow.metadata, studyId, filePath)
        repo.create(metadata) // DB entry created but file doesn't exist

        val result = runCatching { service.getWorkflow(studyId, workflow.metadata.id) }.getOrNull()

        assertNull(result, "Should return null when YAML file is missing")
    }

    @Test
    fun `getWorkflow returns null when YAML file is corrupt`() = runTest {
        val studyId = UUID.randomUUID()
        val workflow = createExampleWorkflow()
        val file = tempDir.resolve("corrupt.yaml").toFile()
        file.writeText("Not valid YAML }}}")

        val metadata = StoredWorkflowMetadata.fromMetadata(workflow.metadata, studyId, file.absolutePath)
        repo.create(metadata)

        val result = runCatching { service.getWorkflow(studyId, workflow.metadata.id) }.getOrNull()

        assertNull(result, "Should return null when YAML is corrupt")
    }

    @Test
    fun `updateWorkflow returns false when workflow doesn't exist`() = runTest {
        val studyId = UUID.randomUUID()
        val workflow = createExampleWorkflow()

        val updated = service.updateWorkflow(studyId, workflow.metadata, workflow)

        assertFalse(updated)
    }

    @Test
    fun `deleteWorkflow returns false when workflow doesn't exist`() = runTest {
        val studyId = UUID.randomUUID()
        val workflowId = UUID.randomUUID()

        val deleted = service.deleteWorkflow(studyId, workflowId)

        assertFalse(deleted)
    }

    private fun createExampleWorkflow(name: String = "Test Workflow"): Workflow {
        return Workflow(
            metadata = WorkflowMetadata(
                id = UUID.randomUUID(),
                name = name,
                description = "Test",
                version = Version(1)
            )
        )
    }
}
