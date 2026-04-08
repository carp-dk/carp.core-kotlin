package dk.cachet.carp.analytics.application.execution.workspace

/**
 * Helper for formatting workspace paths consistently.
 */
object WorkspacePathFormatter
{
    /**
     * Formats a workflow name for use in a filesystem path.
     *
     * - Lowercase
     * - Replace spaces and dashes with underscores
     * - Remove special characters
     *
     * @param name The workflow name
     * @return Formatted name suitable for filesystem use
     */
    fun formatWorkflowName( name: String ): String
    {
        return name
            .replace( " ", "_" )
            .replace( "-", "_" )
            .replace( "[^a-zA-Z0-9_]".toRegex(), "" )
            .lowercase()
    }

    /**
     * Formats a step directory name as used in both the plan and the workspace.
     *
     * Format: {1-based zero-padded index}_{snake_case_step_name}
     * Example: executionIndex=2, stepName="Extract Features" → "03_extract_features"
     */
    fun formatStepDirName( executionIndex: Int, stepName: String ): String
    {
        val paddedIndex = (executionIndex + 1).toString().padStart(2, '0')
        val formattedName = stepName
            .replace(" ", "_")
            .replace("-", "_")
            .replace("[^a-zA-Z0-9_]".toRegex(), "")
            .lowercase()
        return "${paddedIndex}_$formattedName"
    }
}
