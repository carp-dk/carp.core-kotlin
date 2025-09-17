package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
/**
 * Represents the output data produced by a step.
 */
@Serializable
@SerialName("CarpWorkflow.OutputData")
data class OutputDataReference(
    override val name: String,
    override val dataType: String, // format not type 
    val destination: DataLocation
) : DataReference{
    fun validateName() {
        require(name.isNotEmpty()) { "Name cannot be empty" }
    }
    fun validateDataType() {
        require(dataType.isNotEmpty()) { "DataType cannot be empty" }
    }
    fun validateDataLocation() {
        require(destination.segments.isNotEmpty()) { "Output data source path cannot be empty" }
    }
    
    init {
        validateName()
        validateDataType()
        validateDataLocation()
    }
}
