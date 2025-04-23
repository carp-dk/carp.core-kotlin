package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Represents the input data required for a step.
 */
@Serializable
@SerialName("CarpWorkflow.InputData")
data class InputData(
    override val name: String,
    override val dataType: String,
    val source: DataLocation
) : WorkflowData{
    fun validateName() {
        require(name.isNotEmpty()) { "Name cannot be empty" }
    }
    fun validateDataType() {
        require(dataType.isNotEmpty()) { "DataType cannot be empty" }
    }
    fun validateDataLocation() {
        require(source.segments.isNotEmpty()) { "Input data source path cannot be empty" }
    }
    
    init {
        validateName()
        validateDataType()
        validateDataLocation()
    }
}
