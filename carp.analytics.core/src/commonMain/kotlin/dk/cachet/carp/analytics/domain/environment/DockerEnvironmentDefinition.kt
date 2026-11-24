package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * An execution environment backed by a Docker container image.
 *
 * @property image Docker image reference (e.g. `"python:3.11-slim"`).
 */
@Serializable
data class DockerEnvironmentDefinition(
    override val id: UUID,
    override val name: String,
    override val dependencies: List<String> = emptyList(),
    override val environmentVariables: Map<String, String> = emptyMap(),
    val image: String,
) : EnvironmentDefinition
