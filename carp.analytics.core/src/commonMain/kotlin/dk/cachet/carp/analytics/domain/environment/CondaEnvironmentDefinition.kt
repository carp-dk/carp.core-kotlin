package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

@Serializable
data class CondaEnvironmentDefinition(
    override val id: UUID,
    override val name: String,
    override val dependencies: List<String> = listOf(),
    override val environmentVariables: Map<String, String> = emptyMap(),
    val pythonVersion: String = "3.11",
    val channels: List<String> = listOf("conda-forge")
) : EnvironmentDefinition
