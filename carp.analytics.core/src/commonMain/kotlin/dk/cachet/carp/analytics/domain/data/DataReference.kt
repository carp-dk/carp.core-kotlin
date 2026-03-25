package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Base interface for data references in a workflow.
 *
 * A data reference provides logical identification and metadata about data,
 * separate from its physical location or access method.
 *
 * **Contract:**
 * - Implements logical identity (id, name, description)
 * - Includes data schema information
 * - Delegates physical location to [DataLocation]
 * - Validates completeness via validate() method
 */
interface DataReference
{
    /**
     * Unique UUID identifying this reference within a workflow.
     */
    val id: UUID

    /**
     * Human-readable name for this data reference.
     */
    val name: String

    /**
     * Optional longer description of what this reference represents.
     */
    val description: String?

    /**
     * Schema information about the data structure (optional).
     *
     * Null if schema is unknown or not enforced.
     */
    val schema: DataSchema?
}

/**
 * Specification for input data required by a workflow step.
 *
 * **Location Strategy:**
 * - If `stepRef == null`: External input — location must have path/URL/connection
 * - If `stepRef != null`: Data from another step — location path resolved at planning time
 *
 * **Validation:**
 * - External inputs must have non-empty location
 * - Cross-step inputs must have valid stepRef
 * - All location types must be properly configured
 *
 * @property id Unique id for this input within the step
 * @property name Human-readable name (e.g., "raw_data", "model_path")
 * @property description Optional description of what this input represents
 * @property schema Expected data structure / validation rules
 * @property location Where/how to retrieve this data (file, API, environment, etc.)
 * @property stepRef Optional: if set, data comes from another step (cross-step binding)
 * @property required Whether this input is mandatory for step execution
 * @property constraints Optional validation rules (size, format, etc.)
 */
@Serializable
data class InputDataSpec(
    override val id: UUID,
    override val name: String,
    override val description: String? = null,
    override val schema: DataSchema? = null,
    val location: DataLocation,
    val stepRef: String? = null,
    val required: Boolean = true,
    val constraints: DataConstraints? = null
) : DataReference
{
    /**
     * Validates that the input specification is complete and valid.
     *
     * **Validation Rules:**
     * 1. External inputs (stepRef == null) must have non-empty location
     * 2. Cross-step inputs (stepRef != null) must have valid stepRef
     * 3. Each location type has type-specific rules
     * 4. FileLocation: external inputs need path, cross-step inputs can be auto-generated
     * 5. InMemoryLocation: registryKey must be non-empty
     * 6. UrlLocation: url must be non-empty
     * 7. DatabaseLocation: connectionString and table must be non-empty
     * 8. ApiLocation: endpoint must be non-empty
     * 9. StreamLocation: streamAddress and topicOrStream must be non-empty
     *
     * @return ValidationResult.Success if valid, Failure with error list if invalid
     */
    fun validate(): ValidationResult
    {
        val errors = mutableListOf<String>()

        // Validate stepRef if provided
        if ( stepRef != null && stepRef.isBlank() )
        {
            errors.add( "stepRef cannot be blank if provided (input '$name')" )
        }

        // Delegate per-location validation to helper to reduce complexity.
        errors += validateInputLocation( location, stepRef, name )

        return if ( errors.isEmpty() ) ValidationResult.Success else ValidationResult.Failure( errors )
    }
}

/**
 * Specification for output data produced by a workflow step.
 *
 * **Purpose:**
 * Declares what data a step will produce and where it should be stored.
 *
 * **Storage Strategy:**
 * - FileLocation with blank path → auto-generated at planning: `/workspace/outputs/{step}/{name}.{ext}`
 * - FileLocation with path → use as-is (absolute or relative)
 * - Other locations → use as specified
 *
 * **Format:**
 * Can come from two places (in priority order):
 * 1. Explicit format field (overrides location format)
 * 2. Location format (e.g., FileLocation.format)
 *
 * @property id Unique id for this output within the step
 * @property name Human-readable name (e.g., "predictions", "report")
 * @property description Optional description of what this output represents
 * @property schema Structure of the output data
 * @property location Where/how to write this data (file, API, database, etc.)
 * @property format Optional explicit format override (JSON, CSV, PARQUET, etc.)
 */
@Serializable
data class OutputDataSpec(
    override val id: UUID,
    override val name: String,
    override val description: String? = null,
    override val schema: DataSchema? = null,
    val location: DataLocation,
    val format: FileFormat? = null
) : DataReference
{
    /**
     * Validates that the output specification is complete and valid.
     *
     * **Validation Rules:**
     * 1. FileLocation must have a determinable format (from location or format field)
     * 2. InMemoryLocation requires non-empty registryKey
     * 3. UrlLocation is valid (url can be determined at runtime)
     * 4. DatabaseLocation requires non-empty connectionString and table
     * 5. ApiLocation requires non-empty endpoint
     * 6. StreamLocation requires non-empty streamAddress and topicOrStream
     *
     * @return ValidationResult.Success if valid, Failure with error list if invalid
     */
    fun validate(): ValidationResult
    {
        val errors = mutableListOf<String>()

        // Delegate per-location validation to helper to reduce complexity.
        errors += validateOutputLocation( location, format, name )

        return if ( errors.isEmpty() ) ValidationResult.Success else ValidationResult.Failure( errors )
    }
}

// Helper functions to keep validate() methods short and reduce cyclomatic complexity.
private fun validateInputLocation( location: DataLocation, stepRef: String?, name: String ): List<String>
{
    return when ( location )
    {
        is FileLocation -> validateFileInputLocation( location, stepRef, name )
        is InMemoryLocation -> validateInMemoryLocation( location, "Input", name )
        is UrlLocation -> validateUrlLocation( location, "Input", name )
        is DatabaseLocation -> validateDatabaseLocation( location, "Input", name )
        is ApiLocation -> validateApiLocation( location, "Input", name )
        is StreamLocation -> validateStreamLocation( location, "Input", name )
    }
}

private fun validateOutputLocation( location: DataLocation, format: FileFormat?, name: String ): List<String>
{
    return when ( location )
    {
        is FileLocation -> validateFileOutputLocation( location, format, name )
        is InMemoryLocation -> validateInMemoryLocation( location, "Output", name )
        is UrlLocation -> validateUrlLocation( location, "Output", name )
        is DatabaseLocation -> validateDatabaseLocation( location, "Output", name )
        is ApiLocation -> validateApiLocation( location, "Output", name )
        is StreamLocation -> validateStreamLocation( location, "Output", name )
    }
}

// Small helper validators used by both input and output validators to keep complexity low.
private fun validateFileInputLocation( location: FileLocation, stepRef: String?, name: String ): List<String>
{
    return if ( stepRef == null && location.path.isBlank() ) listOf(
        "External input '$name' requires a non-empty file path"
    ) else emptyList()
}

private fun validateFileOutputLocation( location: FileLocation, format: FileFormat?, name: String ): List<String>
{
    val hasFormat = format != null || location.format != FileFormat.BINARY
    return if ( !hasFormat ) listOf(
            "Output '$name' requires a format (either explicit or from location)"
        ) else emptyList()
}

private fun validateInMemoryLocation( location: InMemoryLocation, entity: String, name: String ): List<String>
{
    return if ( location.registryKey.isBlank() ) listOf(
            "$entity '$name' requires a non-empty registry key"
        ) else emptyList()
}

private fun validateUrlLocation( location: UrlLocation, entity: String, name: String ): List<String>
{
    return if ( location.url.isBlank() ) listOf( "$entity '$name' requires a non-empty URL" ) else emptyList()
}

private fun validateDatabaseLocation( location: DatabaseLocation, entity: String, name: String ): List<String>
{
    val errs = mutableListOf<String>()
    if ( location.connectionString.isBlank() ) errs.add(
            "$entity '$name' requires a non-empty database connection string"
        )
    if ( location.table.isBlank() ) errs.add( "$entity '$name' requires a non-empty table name" )
    return errs
}

private fun validateApiLocation( location: ApiLocation, entity: String, name: String ): List<String>
{
    return if ( location.endpoint.isBlank() ) listOf(
            "$entity '$name' requires a non-empty API endpoint"
        ) else emptyList()
}

private fun validateStreamLocation( location: StreamLocation, entity: String, name: String ): List<String>
{
    val errs = mutableListOf<String>()
    if ( location.streamAddress.isBlank() ) errs.add( "$entity '$name' requires a non-empty stream address" )
    if ( location.topicOrStream.isBlank() ) errs.add( "$entity '$name' requires a non-empty topic/stream name" )
    return errs
}

/**
 * Result of a validation operation.
 *
 * **Usage:**
 * ```kotlin
 * val result = inputSpec.validate()
 * when (result) {
 *     ValidationResult.Success -> println("Valid!")
 *     is ValidationResult.Failure -> println("Errors: ${result.errors}")
 * }
 * ```
 */
@Serializable
sealed class ValidationResult
{
    /**
     * Validation passed — all checks successful.
     */
    @Serializable
    object Success : ValidationResult()

    /**
     * Validation failed with one or more errors.
     *
     * @property errors List of error messages
     */
    @Serializable
    data class Failure( val errors: List<String> ) : ValidationResult()

    /**
     * Check if validation failed.
     *
     * @return true if this is Failure, false if Success
     */
    val isFailure: Boolean
        get() = this is Failure
}
