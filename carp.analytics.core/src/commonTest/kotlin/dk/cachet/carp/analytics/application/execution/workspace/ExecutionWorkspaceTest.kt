package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [ExecutionWorkspace] with human-readable step names.
 *
 * Verifies that:
 * - Step names are formatted correctly
 * - Paths use human-readable workflow and step names instead of UUIDs
 * - Index prefixes keep steps sorted
 * - Lookup methods work correctly
 */
class ExecutionWorkspaceTest
{

    @Test
    fun `StepInfo formats directory name with index and name`()
    {
        // Arrange
        val stepInfo = StepInfo(
            id = UUID.randomUUID(),
            name = "Import Data",
            executionIndex = 0
        )

        // Act
        val dirName = stepInfo.toDirectoryName()

        // Assert
        assertEquals( "01_import_data", dirName )
    }

    @Test
    fun `StepInfo handles multiple spaces in name`()
    {
        // Arrange
        val stepInfo = StepInfo(
            id = UUID.randomUUID(),
            name = "Process EEG Signal",
            executionIndex = 1
        )

        // Act
        val dirName = stepInfo.toDirectoryName()

        // Assert
        assertEquals( "02_process_eeg_signal", dirName )
    }

    @Test
    fun `StepInfo handles dashes in name`()
    {
        // Arrange
        val stepInfo = StepInfo(
            id = UUID.randomUUID(),
            name = "Extract-Features",
            executionIndex = 2
        )

        // Act
        val dirName = stepInfo.toDirectoryName()

        // Assert
        assertEquals( "03_extract_features", dirName )
    }

    @Test
    fun `StepInfo pads index to 2 digits`()
    {
        // Arrange
        val steps = listOf(
            StepInfo( UUID.randomUUID(), "First", 0 ),
            StepInfo( UUID.randomUUID(), "Tenth", 9 ),
            StepInfo( UUID.randomUUID(), "Eleventh", 10 )
        )

        // Act & Assert
        assertEquals( "01_first", steps[0].toDirectoryName() )
        assertEquals( "10_tenth", steps[1].toDirectoryName() )
        assertEquals( "11_eleventh", steps[2].toDirectoryName() )
    }

    @Test
    fun `workspace stepDir returns human-readable path`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val stepInfo = StepInfo(
            id = stepId,
            name = "Import Data",
            executionIndex = 0
        )
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace/signal_processing/run_123",
            workflowName = "Signal Processing Pipeline",
            stepInfos = mapOf( stepId to stepInfo )
        )

        // Act
        val stepPath = workspace.stepDir( stepId )

        // Assert
        assertEquals( "steps/01_import_data", stepPath )
    }

    @Test
    fun `workspace stepInputsDir returns correct path`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val stepInfo = StepInfo(
            id = stepId,
            name = "Process Data",
            executionIndex = 1
        )
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf( stepId to stepInfo )
        )

        // Act
        val inputPath = workspace.stepInputsDir( stepId )

        // Assert
        assertEquals( "steps/02_process_data/inputs", inputPath )
    }

    @Test
    fun `workspace stepOutputsDir returns correct path`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val stepInfo = StepInfo(
            id = stepId,
            name = "Extract Features",
            executionIndex = 2
        )
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf( stepId to stepInfo )
        )

        // Act
        val outputPath = workspace.stepOutputsDir( stepId )

        // Assert
        assertEquals( "steps/03_extract_features/outputs", outputPath )
    }

    @Test
    fun `workspace stepLogsDir returns correct path`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val stepInfo = StepInfo(
            id = stepId,
            name = "Generate Report",
            executionIndex = 3
        )
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf( stepId to stepInfo )
        )

        // Act
        val logsPath = workspace.stepLogsDir( stepId )

        // Assert
        assertEquals( "steps/04_generate_report/logs", logsPath )
    }

    @Test
    fun `workspace stepDir throws for unknown step ID`()
    {
        // Arrange
        val knownStep = UUID.randomUUID()
        val unknownStep = UUID.randomUUID()
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf(
                knownStep to StepInfo( knownStep, "Known", 0 )
            )
        )

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            workspace.stepDir( unknownStep )
        }
    }

    @Test
    fun `workspace getStepName returns correct name`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf(
                stepId to StepInfo( stepId, "Import Data", 0 )
            )
        )

        // Act
        val name = workspace.getStepName( stepId )

        // Assert
        assertEquals( "Import Data", name )
    }

    @Test
    fun `workspace getStepName returns null for unknown step`()
    {
        // Arrange
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = emptyMap()
        )

        // Act
        val name = workspace.getStepName( UUID.randomUUID() )

        // Assert
        assertNull( name )
    }

    @Test
    fun `workspace getStepDirName returns formatted name`()
    {
        // Arrange
        val stepId = UUID.randomUUID()
        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf(
                stepId to StepInfo( stepId, "Process EEG", 1 )
            )
        )

        // Act
        val dirName = workspace.getStepDirName( stepId )

        // Assert
        assertEquals( "02_process_eeg", dirName )
    }

    @Test
    fun `workspace getStepIdsInOrder returns steps sorted by execution index`()
    {
        // Arrange
        val step1 = UUID.randomUUID()
        val step2 = UUID.randomUUID()
        val step3 = UUID.randomUUID()

        val workspace = ExecutionWorkspace(
            runId = UUID.randomUUID(),
            executionRoot = "/workspace",
            workflowName = "Pipeline",
            stepInfos = mapOf(
                step3 to StepInfo( step3, "Third", 2 ),
                step1 to StepInfo( step1, "First", 0 ),
                step2 to StepInfo( step2, "Second", 1 )
            )
        )

        // Act
        val orderedIds = workspace.getStepIdsInOrder()

        // Assert
        assertEquals( listOf( step1, step2, step3 ), orderedIds )
    }

    @Test
    fun `workspace toReadableString produces human-friendly output`()
    {
        // Arrange
        val step1 = UUID.randomUUID()
        val step2 = UUID.randomUUID()

        val workspace = ExecutionWorkspace(
            runId = UUID.parse( "550e8400-e29b-41d4-a716-446655440000" ),
            executionRoot = "/workspace",
            workflowName = "Signal Processing Pipeline",
            stepInfos = mapOf(
                step1 to StepInfo( step1, "Import Data", 0 ),
                step2 to StepInfo( step2, "Process EEG", 1 )
            )
        )

        // Act
        val readable = workspace.toReadableString()

        // Assert
        assertTrue( readable.contains( "Signal Processing Pipeline" ) )
        assertTrue( readable.contains( "550e8400-e29b-41d4-a716-446655440000" ) )
        assertTrue( readable.contains( "01_import_data" ) )
        assertTrue( readable.contains( "02_process_eeg" ) )
    }

    @Test
    fun `complete workflow example with multiple steps`()
    {
        // Arrange: Create a realistic signal processing workflow
        val importStep = UUID.randomUUID()
        val preprocessStep = UUID.randomUUID()
        val extractStep = UUID.randomUUID()
        val reportStep = UUID.randomUUID()

        val workspace = ExecutionWorkspace(
            runId = UUID.parse( "a1b2c3d4-0000-0000-0000-000000000001" ),
            executionRoot = "/data/workspaces/signal_processing/run_a1b2c3d4",
            workflowName = "EEG Signal Processing",
            stepInfos = mapOf(
                importStep to StepInfo( importStep, "Validate Input", 0 ),
                preprocessStep to StepInfo( preprocessStep, "Preprocess EEG", 1 ),
                extractStep to StepInfo( extractStep, "Extract Features", 2 ),
                reportStep to StepInfo( reportStep, "Generate Report", 3 )
            )
        )

        // Act: Verify complete path construction
        val importPath = workspace.stepOutputsDir( importStep )
        val preprocessPath = workspace.stepInputsDir( preprocessStep )
        val extractPath = workspace.stepOutputsDir( extractStep )
        val reportPath = workspace.stepLogsDir( reportStep )

        // Assert: Paths should be human-readable and in order
        assertEquals( "steps/01_validate_input/outputs", importPath )
        assertEquals( "steps/02_preprocess_eeg/inputs", preprocessPath )
        assertEquals( "steps/03_extract_features/outputs", extractPath )
        assertEquals( "steps/04_generate_report/logs", reportPath )

        // Verify ordering
        val orderedSteps = workspace.getStepIdsInOrder()
        assertEquals( listOf( importStep, preprocessStep, extractStep, reportStep ), orderedSteps )

        // Verify human-readable output
        val readable = workspace.toReadableString()
        assertTrue( readable.contains( "EEG Signal Processing" ) )
        assertTrue( readable.contains( "01_validate_input" ) )
        assertTrue( readable.contains( "02_preprocess_eeg" ) )
        assertTrue( readable.contains( "03_extract_features" ) )
        assertTrue( readable.contains( "04_generate_report" ) )
    }
}
