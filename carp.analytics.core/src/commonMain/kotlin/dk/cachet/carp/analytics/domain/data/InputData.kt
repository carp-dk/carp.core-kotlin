package dk.cachet.carp.analytics.domain.data

/**
 * Represents the input data required for a step.
 */
data class InputData(
    override val name: String, // Name of the input data
    override val dataType: String, // TODO: Change to KClass inheriting from CARP Data
    val source: DataLocation
): AbstractData(name, dataType, source){
    init {
        validateName()
        validateDataType()
        validateDataLocation()
    }
}
