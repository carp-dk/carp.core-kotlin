package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Base interface for data references in a workflow.
 * A data reference provides logical identification and metadata about data,
 * separate from its physical location or access method.
 */
interface DataReference
{
    val id: UUID
    val name: String
    val description: String?
    val schema: DataSchema?
}

/**
 * Specification for input data required by a workflow step.
 *
 * @property id Unique id for this input
 * @property name Human-readable name
 * @property description Optional description of what this input represents
 * @property schema Expected data structure
 * @property source Where and how to retrieve this data
 * @property required Whether this input is mandatory for step execution
 * @property constraints Validation rules for the input data
 */
@Serializable
data class InputDataSpec(
    override val id: UUID,
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
        errors += validateSource( source )

        return if ( errors.isEmpty() ) ValidationResult.Success else ValidationResult.Failure( errors )
    }

    private fun validateSource( source: DataSource ): List<String>
    {
        return when ( source )
        {
            is FileSystemSource -> validateFileSystemSource( source )
            is UrlSource -> validateUrlSource( source )
            is DatabaseSource -> validateDatabaseSource( source )
            is InMemorySource -> validateInMemorySource( source )
            is ApiSource -> validateApiSource( source )
            is StreamSource -> validateStreamSource( source )
            is StepOutputSource -> validateStepOutputSource()
        }
    }

    private fun validateFileSystemSource( source: FileSystemSource ): List<String>
    {
        return if ( source.path.isBlank() )
            listOf("FileSystemSource path cannot be blank")
        else
            emptyList()
    }

    private fun validateUrlSource( source: UrlSource ): List<String>
    {
        return if ( source.url.isBlank() )
            listOf("UrlSource URL cannot be blank")
        else
            emptyList()
    }

    private fun validateDatabaseSource( source: DatabaseSource ): List<String>
    {
        val errors = mutableListOf<String>()
        if ( source.connectionString.isBlank() )
        {
            errors.add("DatabaseSource connectionString cannot be blank")
        }
        if ( source.query.isBlank() )
        {
            errors.add("DatabaseSource query cannot be blank")
        }
        return errors
    }

    private fun validateInMemorySource( source: InMemorySource ): List<String>
    {
        return if ( source.registryKey.isBlank() )
            listOf("InMemorySource registryKey cannot be blank")
        else
            emptyList()
    }

    private fun validateApiSource( source: ApiSource ): List<String>
    {
        return if ( source.endpoint.isBlank() )
            listOf("ApiSource endpoint cannot be blank")
        else
            emptyList()
    }

    private fun validateStreamSource( source: StreamSource ): List<String>
    {
        return if ( source.streamId.isBlank() )
            listOf("StreamSource streamId cannot be blank")
        else
            emptyList()
    }

    private fun validateStepOutputSource(): List<String>
    {
        // UUID validation is handled by UUID constructor
        return emptyList()
    }
}

/**
 * Specification for output data produced by a workflow step.
 *
 * @property id Unique id for this output
 * @property name Human-readable name
 * @property description Optional description of what this output represents
 * @property schema Structure of the output data
 * @property destination Where and how to write this data
 * @property format Optional override for serialization format
 */
@Serializable
data class OutputDataSpec(
    override val id: UUID,
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

    val isFailure: Boolean
        get()
        {
            return this is Failure
        }
}
