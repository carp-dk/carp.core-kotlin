package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.application.WorkflowStorageService
import dk.cachet.carp.analytics.domain.workflow.*
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.test.*
import java.nio.file.Path
import java.io.FileNotFoundException

class FileSystemWorkflowStorageServiceTest {

    private lateinit var storage: WorkflowStorageService
    private lateinit var tempDir: Path

    @BeforeTest
    fun setup() {
        tempDir = createTempDirectory("workflow_yaml_test_")
        storage = FileSystemWorkflowStorageService(tempDir.toFile())
    }

    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `write and read a valid Workflow`() = runTest {
        val workflow = createExampleWorkflow()

        val path = storage.writeWorkflowToFile(workflow)
        val loaded = storage.readWorkflowFromFile(path)

        assertEquals(workflow.metadata.name, loaded.metadata.name)
        assertEquals(workflow.metadata.id, loaded.metadata.id)
    }

    @Test
    fun `reading a missing file throws FileNotFoundException`() = runTest {
        val missingPath = tempDir.resolve("nonexistent/study1.yaml").toFile()

        assertFailsWith<FileNotFoundException> {
            storage.readWorkflowFromFile(missingPath.absolutePath)
        }
    }

    @Test
    fun `reading a corrupt file throws a parsing error`() = runTest {
        val corruptFile = tempDir.resolve("corrupt.yaml").toFile()
        corruptFile.writeText("This is not valid YAML { : [ what }")

        val exception = assertFailsWith<Exception> {
            storage.readWorkflowFromFile(corruptFile.absolutePath)
        }

        assertTrue(
            exception.message?.contains("parsing", ignoreCase = true) == true ||
                    exception.message?.contains("expected", ignoreCase = true) == true ||
                    exception.message?.contains("unexpected", ignoreCase = true) == true,
            "Unexpected parsing error message: ${exception.message}"
        )
    }

    private fun createExampleWorkflow(): Workflow {
        return Workflow(
            metadata = WorkflowMetadata(
                id = UUID.randomUUID(),
                name = "YAML Roundtrip Test",
                description = "Test description",
                version = Version(1)
            )
        )
    }
}
