package dk.cachet.carp.analytics.domain.data

/**
 * Represents the output data produced by a step.
 */
data class OutputData(
    override val name: String, // Name of the output data
    override val dataType: String, // TODO: Change to KClass inheriting from CARP Data
    val destination: DataLocation 
) : AbstractData(name, dataType, destination){
    init {
        validateName()
        validateDataType()
        validateDataLocation()
    }
}