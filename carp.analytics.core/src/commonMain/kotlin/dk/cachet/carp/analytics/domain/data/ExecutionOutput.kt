package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

/**
 * A concrete representation of an output produced by a step in a workflow.
 *
 * Each [ExecutionOutput] corresponds to a declared [OutputDataReference] in the workflow definition
 * and describes the actual location and data format used during runtime.
 *
 * @param name The name of the output, matching the reference defined in the workflow.
 * @param dataType A description of the data type (e.g., CSV, JSON, parquet).
 * @param location A location descriptor indicating where the data was stored (e.g., file path, URI).
 */
@Serializable
data class ExecutionOutput(
    val name: String,              // matches output ref name
    val dataType: String,
    val location: DataLocation     // where it was saved
)
