package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.application.WorkflowStorageService
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.infrastructure.parser.WorkflowYaml
import dk.cachet.carp.analytics.infrastructure.parser.WorkflowYamlParser
import java.io.File

class FileSystemWorkflowStorageService(
    private val rootDirectory: File
) : WorkflowStorageService {

    override suspend fun writeWorkflowToFile(workflow: Workflow): String {
        val studyId = workflow.metadata.id.toString()
        val workflowId = workflow.metadata.id.toString()
        val file = File(rootDirectory, "$studyId/$workflowId.yaml")

        file.parentFile.mkdirs()
        file.writeText(WorkflowYaml.encodeToString(Workflow.serializer(), workflow))

        return file.absolutePath
    }

    override suspend fun readWorkflowFromFile(filePath: String): Workflow {
        val file = File(filePath)
        return WorkflowYamlParser.fromString(file.readText())
    }
}
