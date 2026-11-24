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
 * Data constraints for validating input/output values.
 *
 * Optional validation rules applied at execution time.
 *
 * @property minSize Minimum data size (bytes) — null for no limit
 * @property maxSize Maximum data size (bytes) — null for no limit
 * @property allowedFormats List of allowed formats — empty for no restriction
 * @property customValidation Custom validation rules (regex patterns, format validators, etc.)
 */
@Serializable
data class DataConstraints(
    val minSize: Long? = null,
    val maxSize: Long? = null,
    val allowedFormats: List<String> = emptyList(),
    val customValidation: Map<String, String> = emptyMap()
)
