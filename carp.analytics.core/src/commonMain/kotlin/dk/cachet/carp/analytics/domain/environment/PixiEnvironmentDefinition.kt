package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * A Pixi environment as authored.
 *
 * @property lockFile A `pixi.lock`, relative to the workflow file, to install from;
 *   null resolves [dependencies] at setup.
 */
@Serializable
data class PixiEnvironmentDefinition(
    override val id: UUID,
    override val name: String,
    override val dependencies: List<String> = listOf(),
    override val environmentVariables: Map<String, String> = emptyMap(),
    val pythonVersion: String = "3.11",
    val featureDeps: Map<String, List<String>> = emptyMap(),
    val channels: List<String> = listOf("conda-forge"),
    val lockFile: String? = null
) : EnvironmentDefinition
