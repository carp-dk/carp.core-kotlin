package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

/**
 * Base interface for data references in a workflow.
 * A data reference provides logical identification and metadata about data,
 * separate from its physical location or access method.
 */
interface DataReference
{
    val identifier: String
    val name: String
    val description: String?
    val schema: DataSchema?
}

/**
 * Specification for input data required by a workflow step.
 *
 * @property identifier Unique identifier for this input
 * @property name Human-readable name
 * @property description Optional description of what this input represents
 * @property schema Expected data structure
 * @property source Where and how to retrieve this data
 * @property required Whether this input is mandatory for step execution
 * @property constraints Validation rules for the input data
 */
@Serializable
data class InputDataSpec(
    override val identifier: String,
    override val name: String,
    override val description: String? = null,
    override val schema: DataSchema? = null,
    val source: DataSource,
    val required: Boolean = true,
    val constraints: DataConstraints? = null
) : DataReference
{
    /**
     * Validates that the data source configuration is complete and valid.
     */
    fun validate(): ValidationResult
    {
        val errors = mutableListOf<String>()

        if ( identifier.isBlank() )
        {
            errors.add("Identifier cannot be blank")
        }

        errors += validateSource( source )

        return if ( errors.isEmpty() ) ValidationResult.Success else ValidationResult.Failure( errors )
    }

    private fun validateSource( source: DataSource ): List<String>
    {
        val errors = mutableListOf<String>()

        when ( source )
        {
            is FileSystemSource ->
            {
                if ( source.path.isBlank() )
                {
                    errors.add("FileSystemSource path cannot be blank")
                }
            }

            is UrlSource ->
            {
                if ( source.url.isBlank() )
                {
                    errors.add("UrlSource URL cannot be blank")
                }
            }

            is DatabaseSource ->
            {
                if ( source.connectionString.isBlank() )
                {
                    errors.add("DatabaseSource connectionString cannot be blank")
                }
                if ( source.query.isBlank() )
                {
                    errors.add("DatabaseSource query cannot be blank")
                }
            }

            is InMemorySource ->
            {
                if ( source.registryKey.isBlank() )
                {
                    errors.add("InMemorySource registryKey cannot be blank")
                }
            }

            is ApiSource ->
            {
                if ( source.endpoint.isBlank() )
                {
                    errors.add("ApiSource endpoint cannot be blank")
                }
            }

            is StreamSource ->
            {
                if ( source.streamId.isBlank() )
                {
                    errors.add("StreamSource streamId cannot be blank")
                }
            }
        }

        return errors
    }
}

/**
 * Specification for output data produced by a workflow step.
 *
 * @property identifier Unique identifier for this output
 * @property name Human-readable name
 * @property description Optional description of what this output represents
 * @property schema Structure of the output data
 * @property destination Where and how to write this data
 * @property format Optional override for serialization format
 */
@Serializable
data class OutputDataSpec(
    override val identifier: String,
    override val name: String,
    override val description: String? = null,
    override val schema: DataSchema? = null,
    val destination: DataDestination,
    val format: FileFormat? = null
) : DataReference
{
    /**
     * Validates that the destination configuration is complete and valid.
     */
    fun validate(): ValidationResult
    {
        val errors = mutableListOf<String>()

        if ( identifier.isBlank() )
        {
            errors.add("Identifier cannot be blank")
        }

        errors += validateDestination( destination )

        return if ( errors.isEmpty() ) ValidationResult.Success else ValidationResult.Failure( errors )
    }

    private fun validateDestination( destination: DataDestination ): List<String>
    {
        val errors = mutableListOf<String>()

        when ( destination )
        {
            is FileDestination ->
            {
                if ( destination.path.isBlank() )
                {
                    errors.add("FileDestination path cannot be blank")
                }
            }

            is RegistryDestination ->
            {
                if ( destination.key.isBlank() )
                {
                    errors.add("RegistryDestination key cannot be blank")
                }
            }

            is DatabaseDestination ->
            {
                if ( destination.connectionString.isBlank() )
                {
                    errors.add("DatabaseDestination connectionString cannot be blank")
                }
                if ( destination.table.isBlank() )
                {
                    errors.add("DatabaseDestination table cannot be blank")
                }
            }

            is ApiDestination ->
            {
                if ( destination.endpoint.isBlank() )
                {
                    errors.add("ApiDestination endpoint cannot be blank")
                }
            }

            is StreamDestination ->
            {
                if ( destination.streamId.isBlank() )
                {
                    errors.add("StreamDestination streamId cannot be blank")
                }
            }
        }

        return errors
    }
}

/**
 * Result of a validation operation.
 */
@Serializable
sealed class ValidationResult
{
    /** Validation passed */
    @Serializable
    object Success : ValidationResult()

    /** Validation failed with errors */
    @Serializable
    data class Failure( val errors: List<String> ) : ValidationResult()

    val isSuccess: Boolean
        get()
        {
            return this is Success
        }

    val isFailure: Boolean
        get()
        {
            return this is Failure
        }
}
