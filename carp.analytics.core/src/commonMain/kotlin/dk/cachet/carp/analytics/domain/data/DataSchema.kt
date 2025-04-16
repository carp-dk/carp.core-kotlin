package dk.cachet.carp.analytics.domain.data

import dk.cachet.carp.common.application.data.Data

data class DataSchema<T : Data>(
    val name: String,
    val fields: Map<String, T> // Key: field name, Value: field type
){
    init {
        require(name.isNotBlank()) { "Name cannot be empty" }
    }
}
