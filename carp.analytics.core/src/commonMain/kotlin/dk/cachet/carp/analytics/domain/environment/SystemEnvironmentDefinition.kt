package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Represents the host system environment — no package manager, no isolated Python.
 *
 * For system executables present in the PATH,
 * (e.g. `echo`, `bash`, `curl`, standard OS utilities).
 *
 */
@Serializable
data class SystemEnvironmentDefinition(
    override val id: UUID,
    override val name: String,
    override val dependencies: List<String> = emptyList(),
    override val environmentVariables: Map<String, String> = emptyMap(),
) : EnvironmentDefinition

