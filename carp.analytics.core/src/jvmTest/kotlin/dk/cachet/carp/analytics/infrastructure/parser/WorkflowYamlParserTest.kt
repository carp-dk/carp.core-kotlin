package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.execution.ExecutionContext
import dk.cachet.carp.analytics.domain.process.CommandLineProcess
import dk.cachet.carp.analytics.domain.process.CommandTemplate
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.infrastructure.parser.WorkflowYamlParser
import dk.cachet.carp.analytics.infrastructure.util.SharedYaml
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WorkflowYamlParserTest {

    @Test
    fun `can serialize and deserialize a simple workflow`() {
        // Arrange
        val commandTemplate = CommandTemplate("cmd /c move {0} {1}")
        val process = CommandLineProcess(
            name = "Move File",
            executionContext = ExecutionContext(envVariables = mapOf("cmd" to "cmd")),
            commandTemplate = commandTemplate,
            arguments = listOf("sourceFile", "destinationFile")
        )

        // Create a one-step workflow
        val step = Step(name = "Move File Step", description = "Moves a file from source to destination",
            inputData = emptyList(), outputData = null, process = process)
        val workflow = Workflow(name = "File Move Workflow", description = "Moves a file from source to destination")

        // Add the step to the workflow
        workflow.addStep(step)

        // Act

        val yamlString = SharedYaml.encodeToString(Workflow.serializer(), workflow)
        val restoredWorkflow = WorkflowYamlParser.fromString(yamlString)

        // Assert
        assertEquals(workflow, restoredWorkflow)
    }
}
