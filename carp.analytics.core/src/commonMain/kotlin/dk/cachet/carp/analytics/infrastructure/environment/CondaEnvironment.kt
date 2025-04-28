package dk.cachet.carp.analytics.infrastructure.environment

import dk.cachet.carp.analytics.domain.environment.Environment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("CondaEnvironment")
@Serializable
data class CondaEnvironment(
    override val name: String,
    override val dependencies: List<String>,
    val channels: List<String> = listOf("defaults"),
    val pythonVersion: String? = null
) : Environment