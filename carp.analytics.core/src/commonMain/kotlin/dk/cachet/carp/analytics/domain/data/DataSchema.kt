package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.Serializable

/**
 * Describes the structure and format of data.
 * Used for validation and compatibility checking between workflow steps.
 *
 * @property format The file or serialization format
 * @property columns Column specifications for tabular data
 * @property jsonSchema JSON Schema definition for structured data
 * @property encoding Character encoding (e.g., "UTF-8")
 * @property compression Compression algorithm used (e.g., "gzip", "snappy")
 */
@Serializable
data class DataSchema(
    val format: FileFormat,
    val columns: List<ColumnSpec>? = null,
    val jsonSchema: String? = null,
    val encoding: String = "UTF-8",
    val compression: String? = null
)
{
    /**
     * Validates whether this schema is compatible with another schema.
     * Used to check if data from one step can be consumed by another.
     */
    fun isCompatibleWith( other: DataSchema ): Boolean
    {
        if (format != other.format) return false
        if (columns != null && other.columns != null)
        {
            val thisColumnNames = columns.map { it.name }.toSet()
            val requiredOtherColumns = other.columns.filter { !it.nullable }
            return requiredOtherColumns.all { it.name in thisColumnNames }
        }
        return true
    }
}

/**
 * Specification for a column in tabular data.
 *
 * @property name The column name
 * @property dataType The CARP data type for this column
 * @property nullable Whether null values are allowed
 * @property description Human-readable description
 * @property defaultValue Default value for missing data (as string)
 */
@Serializable
data class ColumnSpec(
    val name: String,
    val dataType: DataType,
    val nullable: Boolean = true,
    val description: String? = null,
    val defaultValue: String? = null
)

/**
 * Constraints for validating data quality.
 *
 * @property minRows Minimum number of rows expected
 * @property maxRows Maximum number of rows allowed
 * @property requiredColumns Columns that must be present
 * @property uniqueColumns Columns that must have unique values
 * @property notNullColumns Columns that cannot contain null values
 */
@Serializable
data class DataConstraints(
    val minRows: Long? = null,
    val maxRows: Long? = null,
    val requiredColumns: List<String> = emptyList(),
    val uniqueColumns: List<String> = emptyList(),
    val notNullColumns: List<String> = emptyList()
)
