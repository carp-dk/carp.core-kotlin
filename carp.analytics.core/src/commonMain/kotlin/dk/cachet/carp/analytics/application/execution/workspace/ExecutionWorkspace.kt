package dk.cachet.carp.analytics.application.execution.workspace

import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.INPUTS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.LOGS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.OUTPUTS_DIR
import dk.cachet.carp.analytics.application.execution.workspace.WorkspaceLayoutRules.STEPS_DIR
import dk.cachet.carp.common.application.UUID

/**
 * Represents a run-scoped execution workspace.
 *
 * This is a pure, deterministic workspace model that provides logical path construction
 * without any filesystem access or IO operations. All paths are relative to the
 * [executionRoot].
 *
 * **Workspace Layout:**
 * ```
 * executionRoot/
 *   steps/
 *     01_import_data/
 *       inputs/         ← Input data files
 *       outputs/        ← Output artifacts
 *       logs/           ← Execution logs
 *     02_process_eeg/
 *       inputs/
 *       outputs/
 *       logs/
 * ```
 *
 * The workspace is **run-scoped** — each execution run has its own isolated workspace
 * directory structure to prevent cross-run contamination.
 *
 * **Human-Readable Paths:**
 * The [stepInfos] map allows conversion from step UUIDs to human-readable names.
 * This makes debugging and monitoring much easier (reading "01_import_data" instead of
 * a UUID).
 *
 * @param runId Unique identifier for this execution run (UUID)
 * @param executionRoot Workspace root directory path (typically absolute filesystem path)
 * @param workflowName Human-readable workflow name
 * @param stepInfos Map of step UUID to StepInfo (name, index, etc.)
 */
data class ExecutionWorkspace(
    val runId: UUID,
    val executionRoot: String,
    val workflowName: String,
    val stepInfos: Map<UUID, StepInfo> = emptyMap()
)
{
    /**
     * Returns the step directory path for the given step ID.
     *
     * Uses human-readable name with execution index prefix.
     *
     * @param stepId The step identifier UUID
     * @return Relative path "steps/{index}_{stepName}" (e.g., "steps/01_import_data")
     * @throws IllegalArgumentException if stepId is not in this workspace
     */
    fun stepDir( stepId: UUID ): String
    {
        val stepInfo = stepInfos[stepId]
            ?: throw IllegalArgumentException(
                "Step $stepId not found in workspace. Available steps: ${stepInfos.keys}"
            )
        return "$STEPS_DIR/${stepInfo.toDirectoryName()}"
    }

    /**
     * Returns the step inputs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID
     * @return Relative path "steps/{index}_{stepName}/inputs" (e.g., "steps/01_import_data/inputs")
     * @throws IllegalArgumentException if stepId is not in this workspace
     */
    fun stepInputsDir( stepId: UUID ): String
    {
        return "${stepDir( stepId )}/$INPUTS_DIR"
    }

    /**
     * Returns the step outputs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID
     * @return Relative path "steps/{index}_{stepName}/outputs"
     * @throws IllegalArgumentException if stepId is not in this workspace
     */
    fun stepOutputsDir( stepId: UUID ): String
    {
        return "${stepDir( stepId )}/$OUTPUTS_DIR"
    }

    /**
     * Returns the step logs directory path for the given step ID.
     *
     * @param stepId The step identifier UUID
     * @return Relative path "steps/{index}_{stepName}/logs"
     * @throws IllegalArgumentException if stepId is not in this workspace
     */
    fun stepLogsDir( stepId: UUID ): String
    {
        return "${stepDir( stepId )}/$LOGS_DIR"
    }

    /**
     * Returns the human-readable name for a given step ID.
     *
     * @param stepId The step identifier UUID
     * @return The step name (e.g., "Import Data"), or null if not found
     */
    fun getStepName( stepId: UUID ): String?
    {
        return stepInfos[stepId]?.name
    }

    /**
     * Returns the human-readable directory name for a given step ID.
     *
     * @param stepId The step identifier UUID
     * @return The formatted directory name (e.g., "01_import_data"), or null if not found
     */
    fun getStepDirName( stepId: UUID ): String?
    {
        return stepInfos[stepId]?.toDirectoryName()
    }

    /**
     * Returns all step UUIDs in execution order.
     *
     * @return List of step UUIDs sorted by execution index
     */
    fun getStepIdsInOrder(): List<UUID>
    {
        return stepInfos.values
            .sortedBy { it.executionIndex }
            .map { it.id }
    }

    /**
     * Returns a human-readable representation of the workspace structure.
     *
     * Useful for logging and debugging.
     *
     * Example output:
     * ```
     * Workspace: signal_processing_pipeline/run_550e8400-e29b-41d4-a716-446655440000
     *   01_import_data
     *   02_process_eeg
     *   03_extract_features
     * ```
     *
     * @return Human-readable workspace structure
     */
    fun toReadableString(): String
    {
        val sb = StringBuilder()
        sb.append( "Workspace: $workflowName/run_$runId\n" )
        getStepIdsInOrder().forEach { stepId ->
            val dirName = getStepDirName( stepId )
            sb.append( "  $dirName\n" )
        }
        return sb.toString()
    }
}

/**
 * Information about a step for human-readable path construction.
 *
 * Holds both the UUID (for lookups) and human-readable information (for paths and logging).
 *
 * @param id The step's UUID identifier (for internal lookups)
 * @param name The step's human-readable name from metadata
 * @param executionIndex Zero-based index of this step in execution order (used for sorting)
 */
data class StepInfo(
    val id: UUID,
    val name: String,
    val executionIndex: Int
)
{
    /**
     * Returns the human-readable directory name for this step.
     *
     * Format: "{index}_{name}" where:
     * - Index is zero-padded to 2 digits (01, 02, 03...)
     * - Name is lowercased and spaces/dashes replaced with underscores
     *
     * Examples:
     * - "01_import_data"
     * - "02_process_eeg"
     * - "03_extract_features"
     *
     * @return Human-readable step directory name suitable for filesystem use
     */
    fun toDirectoryName(): String
    {
        val paddedIndex = ( executionIndex + 1 ).toString().padStart( 2, '0' )
        val sanitizedName = name
            .replace( " ", "_" )
            .replace( "-", "_" )
            .lowercase()
        return "${paddedIndex}_$sanitizedName"
    }
}
