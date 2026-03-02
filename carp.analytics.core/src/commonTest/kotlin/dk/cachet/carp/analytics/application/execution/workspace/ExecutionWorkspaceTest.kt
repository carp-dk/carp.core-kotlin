package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Tests for [ExecutionWorkspace] to ensure deterministic behaviour with UUID step identifiers.
 */
class ExecutionWorkspaceTest
{
    private val testRunId = UUID.randomUUID()
    private val testExecutionRoot = "test-execution-root"
    private val workspace = ExecutionWorkspace(testRunId, testExecutionRoot)

    @Test
    fun stepDir_produces_deterministic_paths()
    {
        val stepId = UUID.randomUUID()

        val result1 = workspace.stepDir(stepId)
        val result2 = workspace.stepDir(stepId)

        assertEquals("steps/$stepId", result1)
        assertEquals(result1, result2, "stepDir should be deterministic")
    }

    @Test
    fun stepInputsDir_produces_correct_structure()
    {
        val stepId = UUID.randomUUID()

        val result = workspace.stepInputsDir(stepId)

        assertEquals("steps/$stepId/inputs", result)
    }

    @Test
    fun stepOutputsDir_produces_correct_structure()
    {
        val stepId = UUID.randomUUID()

        val result = workspace.stepOutputsDir(stepId)

        assertEquals("steps/$stepId/outputs", result)
    }

    @Test
    fun stepLogsDir_produces_correct_structure()
    {
        val stepId = UUID.randomUUID()

        val result = workspace.stepLogsDir(stepId)

        assertEquals("steps/$stepId/logs", result)
    }

    @Test
    fun all_subdirectory_functions_are_deterministic()
    {
        val stepId = UUID.randomUUID()

        val stepDir1 = workspace.stepDir(stepId)
        val stepDir2 = workspace.stepDir(stepId)
        val inputsDir1 = workspace.stepInputsDir(stepId)
        val inputsDir2 = workspace.stepInputsDir(stepId)
        val outputsDir1 = workspace.stepOutputsDir(stepId)
        val outputsDir2 = workspace.stepOutputsDir(stepId)
        val logsDir1 = workspace.stepLogsDir(stepId)
        val logsDir2 = workspace.stepLogsDir(stepId)

        assertEquals(stepDir1, stepDir2)
        assertEquals(inputsDir1, inputsDir2)
        assertEquals(outputsDir1, outputsDir2)
        assertEquals(logsDir1, logsDir2)
    }

    @Test
    fun different_step_uuids_produce_different_paths()
    {
        val stepId1 = UUID.randomUUID()
        val stepId2 = UUID.randomUUID()

        val stepDir1 = workspace.stepDir(stepId1)
        val stepDir2 = workspace.stepDir(stepId2)
        val inputsDir1 = workspace.stepInputsDir(stepId1)
        val inputsDir2 = workspace.stepInputsDir(stepId2)
        val outputsDir1 = workspace.stepOutputsDir(stepId1)
        val outputsDir2 = workspace.stepOutputsDir(stepId2)
        val logsDir1 = workspace.stepLogsDir(stepId1)
        val logsDir2 = workspace.stepLogsDir(stepId2)

        assertNotEquals(stepDir1, stepDir2, "Different UUIDs should produce different step directories")
        assertNotEquals(inputsDir1, inputsDir2, "Different UUIDs should produce different input directories")
        assertNotEquals(outputsDir1, outputsDir2, "Different UUIDs should produce different output directories")
        assertNotEquals(logsDir1, logsDir2, "Different UUIDs should produce different log directories")
    }

    @Test
    fun paths_contain_proper_uuid_string_representation()
    {
        val stepId = UUID.randomUUID()
        val expectedUuidString = stepId.toString()

        val stepDir = workspace.stepDir(stepId)
        val inputsDir = workspace.stepInputsDir(stepId)
        val outputsDir = workspace.stepOutputsDir(stepId)
        val logsDir = workspace.stepLogsDir(stepId)

        assertTrue(stepDir.contains(expectedUuidString), "stepDir should contain UUID string")
        assertTrue(inputsDir.contains(expectedUuidString), "inputsDir should contain UUID string")
        assertTrue(outputsDir.contains(expectedUuidString), "outputsDir should contain UUID string")
        assertTrue(logsDir.contains(expectedUuidString), "logsDir should contain UUID string")
    }

    @Test
    fun workspace_fields_are_preserved()
    {
        assertEquals(testRunId, workspace.runId)
        assertEquals(testExecutionRoot, workspace.executionRoot)
    }

    @Test
    fun data_class_equality_works_correctly()
    {
        val workspace1 = ExecutionWorkspace(testRunId, testExecutionRoot)
        val workspace2 = ExecutionWorkspace(testRunId, testExecutionRoot)
        val workspace3 = ExecutionWorkspace(UUID.randomUUID(), testExecutionRoot)
        val workspace4 = ExecutionWorkspace(testRunId, "different-root")

        assertEquals(workspace1, workspace2, "Same content should be equal")
        assertNotEquals(workspace1, workspace3, "Different runId should not be equal")
        assertNotEquals(workspace1, workspace4, "Different executionRoot should not be equal")
    }

    @Test
    fun uuid_based_paths_are_safe_by_design()
    {
        // UUIDs are inherently safe - they don't contain path traversal characters
        // This test verifies that UUID.toString() produces safe path components
        val stepId = UUID.randomUUID()
        val uuidString = stepId.toString()

        assertFalse(uuidString.contains(".."), "UUID string should not contain path traversal")
        assertFalse(uuidString.contains("/"), "UUID string should not contain forward slash")
        assertFalse(uuidString.contains("\\"), "UUID string should not contain backslash")

        // Verify the paths are safe
        val stepDir = workspace.stepDir(stepId)
        val inputsDir = workspace.stepInputsDir(stepId)
        val outputsDir = workspace.stepOutputsDir(stepId)
        val logsDir = workspace.stepLogsDir(stepId)

        // All paths should start with "steps/" and not contain dangerous characters
        assertTrue(stepDir.startsWith("steps/"))
        assertTrue(inputsDir.startsWith("steps/"))
        assertTrue(outputsDir.startsWith("steps/"))
        assertTrue(logsDir.startsWith("steps/"))
    }
}
