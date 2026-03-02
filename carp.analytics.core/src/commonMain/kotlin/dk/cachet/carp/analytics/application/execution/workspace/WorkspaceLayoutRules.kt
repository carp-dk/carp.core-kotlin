package dk.cachet.carp.analytics.application.execution.workspace

/**
 * Central place for canonical workspace layout constants.
 *
 * Defines the standard directory structure for execution workspaces:
 * ```
 * executionRoot/
 *   steps/
 *     {stepId}/
 *       inputs/
 *       outputs/
 *       logs/
 * ```
 */
object WorkspaceLayoutRules
{
    const val STEPS_DIR = "steps"
    const val INPUTS_DIR = "inputs"
    const val OUTPUTS_DIR = "outputs"
    const val LOGS_DIR = "logs"
}
