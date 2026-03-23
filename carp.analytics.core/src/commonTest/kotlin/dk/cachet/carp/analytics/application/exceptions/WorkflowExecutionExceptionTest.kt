package dk.cachet.carp.analytics.application.exceptions

import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WorkflowExecutionExceptionTest
{

    @Test
    fun `step execution exception stores step id`()
    {
        val stepId = UUID.randomUUID()
        val exception = StepExecutionException(
            message = "Step failed",
            stepId = stepId,
            exitCode = 1
        )

        assertEquals(stepId, exception.stepId)
        assertEquals(1, exception.exitCode)
        assertEquals("Step failed", exception.message)
    }

    @Test
    fun `data resolution exception stores data ref id`()
    {
        val dataRefId = UUID.randomUUID()
        val exception = DataResolutionException(
            message = "Data not found",
            dataRefId = dataRefId
        )

        assertEquals(dataRefId, exception.dataRefId)
        assertEquals("Data not found", exception.message)
    }

    @Test
    fun `environment setup exception stores env id`()
    {
        val envId = UUID.randomUUID()
        val exception = EnvironmentSetupException(
            message = "Env setup failed",
            envId = envId.toString()
        )

        assertEquals(envId.toString(), exception.envId)
    }

    @Test
    fun `artifact collection exception stores artifact id`()
    {
        val artifactId = UUID.randomUUID()
        val exception = ArtefactCollectionException(
            message = "Artifact collection failed",
            artefactId = artifactId
        )

        assertEquals(artifactId, exception.artefactId)
    }

    @Test
    fun `data flow exception stores source and target step ids`()
    {
        val sourceStepId = UUID.randomUUID()
        val targetStepId = UUID.randomUUID()
        val exception = DataFlowException(
            message = "Data flow error",
            sourceStepId = sourceStepId,
            targetStepId = targetStepId
        )

        assertEquals(sourceStepId, exception.sourceStepId)
        assertEquals(targetStepId, exception.targetStepId)
    }

    @Test
    fun `workflow validation exception stores issues`()
    {
        val issues = listOf("Issue 1", "Issue 2", "Issue 3")
        val exception = WorkflowValidationException(
            message = "Validation failed",
            issues = issues
        )

        assertEquals(3, exception.issues.size)
        assertTrue(exception.issues.contains("Issue 1"))
    }

    @Test
    fun `process execution exception stores command and exit code`()
    {
        val exception = ProcessExecutionException(
            message = "Process failed",
            command = "python script.py",
            exitCode = 127
        )

        assertEquals("python script.py", exception.command)
        assertEquals(127, exception.exitCode)
    }

    @Test
    fun `execution io exception stores file path`()
    {
        val exception = ExecutionIOException(
            message = "File read failed",
            filePath = "/path/to/file.txt"
        )

        assertEquals("/path/to/file.txt", exception.filePath)
    }

    @Test
    fun `exceptions are subclasses of workflow execution exception`()
    {
        val exc1: WorkflowExecutionException = StepExecutionException("msg", UUID.randomUUID())
        val exc2: WorkflowExecutionException = DataResolutionException("msg", UUID.randomUUID())
        val exc3: WorkflowExecutionException = EnvironmentSetupException("msg", UUID.randomUUID().toString())

        assertTrue(exc1 is StepExecutionException)
        assertTrue(exc2 is DataResolutionException)
        assertTrue(exc3 is EnvironmentSetupException)
    }

    @Test
    fun `exception with cause preserves cause chain`()
    {
        val cause = IllegalArgumentException("Original cause")
        val exception = StepExecutionException(
            message = "Wrapped exception",
            stepId = UUID.randomUUID(),
            cause = cause
        )

        assertNotNull(exception.cause)
        assertEquals("Original cause", exception.cause?.message)
    }
}
