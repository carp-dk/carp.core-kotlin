package dk.cachet.carp.analytics.domain.data

abstract class AbstractData(
    open val name: String,
    open val dataType: String,
    open val dataLocation: DataLocation
)  {
    fun validateName() {
        require(name.isNotEmpty()) { "Name cannot be empty" }
    }

    fun validateDataType() {
        require(dataType.isNotEmpty()) { "DataType cannot be empty" }
    }

    fun validateDataLocation() {
        require(dataLocation.segments.isNotEmpty()) { "Path cannot be empty" }
    }
}