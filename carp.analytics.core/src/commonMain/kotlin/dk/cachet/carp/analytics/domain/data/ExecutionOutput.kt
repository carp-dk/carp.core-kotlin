package dk.cachet.carp.analytics.domain.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// Named constants for byte-size thresholds (lower-case to match VariableNaming rule)
private const val kb: Long = 1024L
private const val mb: Long = kb * 1024L
private const val gb: Long = mb * 1024L

/**
 * Runtime information about data produced during workflow execution.
 * Represents what actually happened when a step executed, as opposed to
 * OutputDataSpec which represents the design-time specification.
 *
 * @property outputId References the OutputDataSpec identifier
 * @property actualLocation Where the data actually ended up (may differ from planned destination)
 * @property statistics Runtime metrics about the data
 * @property timestamp When this output was produced
 * @property success Whether the output was produced successfully
 * @property errorMessage Error message if an exception occurred
 */
@Serializable
data class ExecutionOutput(
    val outputId: String,
    val actualLocation: DataSource,
    val statistics: DataStatistics,
    val timestamp: Instant,
    val success: Boolean,
    val errorMessage: String? = null
)
{
    /**
     * Whether this output can be used as input for subsequent steps.
     */
    val isValid: Boolean
        get() = success && errorMessage == null
}

/**
 * Statistical information about data collected at runtime.
 *
 * @property rowCount Number of rows/records in the data (for tabular data)
 * @property byteSize Size of the data in bytes
 * @property columnCount Number of columns (for tabular data)
 * @property checksum Data checksum for integrity verification
 * @property customMetrics Additional domain-specific metrics (stored as strings)
 */
@Serializable
data class DataStatistics(
    val rowCount: Long? = null,
    val byteSize: Long? = null,
    val columnCount: Int? = null,
    val checksum: String? = null,
    val customMetrics: Map<String, String> = emptyMap()
)
{
    /**
     * Checks if the statistics meet the given constraints.
     */
    fun satisfiesConstraints( constraints: DataConstraints ): Boolean
    {
        // Check minimum rows
        if ( constraints.minRows != null && rowCount != null && rowCount < constraints.minRows )
        {
            return false
        }

        // Check maximum rows
        if ( constraints.maxRows != null && rowCount != null && rowCount > constraints.maxRows )
        {
            return false
        }

        return true
    }

    /**
     * Returns a human-readable summary of the statistics.
     */
    fun summary(): String
    {
        val parts = mutableListOf<String>()

        if ( rowCount != null )
        {
            parts.add("$rowCount rows")
        }
        if ( columnCount != null )
        {
            parts.add("$columnCount columns")
        }
        if ( byteSize != null )
        {
            parts.add("${formatBytes( byteSize )}")
        }

        return if ( parts.isEmpty() )
        {
            "No statistics available"
        }
        else
        {
            parts.joinToString(", ")
        }
    }

    private fun formatBytes( bytes: Long ): String
    {
        return when
        {
            bytes < kb -> "$bytes B"
            bytes < mb -> "${bytes / kb} KB"
            bytes < gb -> "${bytes / mb} MB"
            else -> "${bytes / gb} GB"
        }
    }
}

/**
 * Collection of execution outputs from a workflow step.
 *
 * @property stepId Identifier of the step that produced these outputs
 * @property outputs List of execution outputs
 * @property duration How long the step took to execute
 */
@Serializable
data class StepExecutionResult(
    val stepId: String,
    val outputs: List<ExecutionOutput>,
    val duration: kotlinx.datetime.DateTimePeriod? = null
)
{
    /** Whether all outputs were produced successfully */
    val allSuccessful: Boolean
        get() = outputs.all { it.success }

    /** Outputs that were produced successfully */
    val successfulOutputs: List<ExecutionOutput>
        get() = outputs.filter { it.success }

    /** Outputs that failed */
    val failedOutputs: List<ExecutionOutput>
        get() = outputs.filter { !it.success }
}
