package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.application.execution.workspace.ExecutionWorkspace
import dk.cachet.carp.analytics.application.plan.InTasksRun
import dk.cachet.carp.analytics.application.plan.PlannedStep
import dk.cachet.carp.analytics.application.plan.ResolvedBindings
import dk.cachet.carp.analytics.application.runtime.CommandResult
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

class StepLogRecorderTest
{
    @Test
    fun recordLogs_forwards_arguments_and_returns_resource()
    {
        val expected = ResourceRef(
            kind = ResourceKind.RELATIVE_PATH,
            value = "steps/logs/combined.log",
            mediaType = "text/plain",
            byteSize = 12L
        )
        val recorder = FakeStepLogRecorder(expected)
        val step = createStep()
        val result = createCommandResult(stdout = "ok", stderr = "")
        val workspace = createWorkspace()

        val recorded = recorder.recordLogs(step, result, workspace)

        assertEquals(expected, recorded)
        assertEquals(step, recorder.lastStep)
        assertEquals(result, recorder.lastResult)
        assertEquals(workspace, recorder.lastWorkspace)
    }

    @Test
    fun recordLogs_can_return_null_when_no_logs_are_written()
    {
        val recorder = FakeStepLogRecorder(null)

        val recorded = recorder.recordLogs(createStep(), createCommandResult(), createWorkspace())

        assertNull(recorded)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun logRecord_preserves_all_constructor_values()
    {
        val stepId = UUID.randomUUID()
        val location = ResourceRef(ResourceKind.URI, "https://logs.example.org/runs/1/step.log")
        val recordedAt = Instant.parse("2026-03-13T10:30:00Z")

        val record = LogRecord(
            stepMetadata = StepMetadata(id = stepId, name = "Test Step"),
            location = location,
            hasStdout = true,
            hasStderr = false,
            recordedAt = recordedAt
        )

        assertEquals(stepId, record.stepMetadata.id)
        assertEquals(location, record.location)
        assertEquals(true, record.hasStdout)
        assertEquals(false, record.hasStderr)
        assertEquals(recordedAt, record.recordedAt)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun logRecord_copy_changes_only_selected_fields()
    {
        val original = LogRecord(
            stepMetadata = StepMetadata("Test Step", UUID.randomUUID() ),
            location = ResourceRef(ResourceKind.RELATIVE_PATH, "steps/a/logs/combined.log"),
            hasStdout = true,
            hasStderr = false,
            recordedAt = Instant.parse("2026-03-13T10:30:00Z")
        )

        val updated = original.copy(hasStderr = true)

        assertNotEquals(original, updated)
        assertEquals(original.stepMetadata, updated.stepMetadata)
        assertEquals(original.location, updated.location)
        assertEquals(original.hasStdout, updated.hasStdout)
        assertEquals(true, updated.hasStderr)
        assertEquals(original.recordedAt, updated.recordedAt)
    }

    private class FakeStepLogRecorder(
        private val toReturn: ResourceRef?
    ) : StepLogRecorder
    {
        var lastStep: PlannedStep? = null
        var lastResult: CommandResult? = null
        var lastWorkspace: ExecutionWorkspace? = null

        override fun recordLogs(
            step: PlannedStep,
            result: CommandResult,
            workspace: ExecutionWorkspace
        ): ResourceRef?
        {
            lastStep = step
            lastResult = result
            lastWorkspace = workspace
            return toReturn
        }
    }

    private fun createStep(): PlannedStep =
        PlannedStep(
            metadata = StepMetadata(
                id = UUID.randomUUID(),
                name = "Test Step"
            ),
            process = InTasksRun(operationId = "op.test"),
            bindings = ResolvedBindings(),
            environmentRef = UUID.randomUUID()
        )

    private fun createCommandResult(
        stdout: String = "",
        stderr: String = ""
    ): CommandResult =
        CommandResult(
            exitCode = 0,
            stdout = stdout,
            stderr = stderr,
            durationMs = 25,
            timedOut = false
        )

    private fun createWorkspace(): ExecutionWorkspace =
        ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "runs/test",
            workflowName = "Test Workflow"
        )
}

