package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.domain.workflow.*
import dk.cachet.carp.common.application.UUID
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WorkflowYamlParserTest {

    @Test
    fun `can serialize and deserialize a simple workflow`() {
        // Arrange
        val commandTemplate = CommandTemplate("cmd /c move {0} {1}")
        val process = CommandLineExternalProcess(
            name = "Move File",
            executionContext = ExecutionContext(envVariables = mapOf("cmd" to "cmd")),
            commandTemplate = commandTemplate,
            args = listOf("sourceFile", "destinationFile"),
            description = "Moves a file from source to destination"
        )

        // Create a one-step workflow using new metadata model
        val step = Step(
            metadata = StepMetadata(
                name = "Move File Step",
                description = "Moves a file from source to destination"
            ),
            inputData = emptyList(),
            outputData = null,
            process = process
        )

        val workflow = Workflow(
            metadata = WorkflowMetadata(
                name = "File Move Workflow",
                description = "Moves a file from source to destination",
                id = UUID.randomUUID()
            )
        )

        workflow.addComponent(step)

        // Act
        val yamlString = WorkflowYaml.encodeToString(Workflow.serializer(), workflow)
        val restoredWorkflow = WorkflowYamlParser.fromString(yamlString)

        // Assert
        assertEquals(workflow, restoredWorkflow)
    }


}
