package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the input data required for a step.
 */
@Serializable
@SerialName("CarpWorkflow.InputData")
data class InputDataReference(
    override val name: String,
    override val dataType: String,
    val source: DataLocation
) : DataReference
{
    fun validateName()
    {
        require(name.isNotEmpty()) { "Name cannot be empty" }
    }
    fun validateDataType()
    {
        require(dataType.isNotEmpty()) { "DataType cannot be empty" }
    }
    fun validateDataLocation()
    {
        require(source.segments.isNotEmpty()) { "Input data source path cannot be empty" }
    }
    init
    {
        validateName()
        validateDataType()
        validateDataLocation()
    }
}
