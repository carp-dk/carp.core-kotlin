package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

@Serializable
data class StepRunDetail(
    val command: List<String>? = null,
    val workingDirectory: String? = null,   // relative path

    val exitCode: Int? = null,

    val stdout: ResourceRef? = null,
    val stderr: ResourceRef? = null,
    val log: ResourceRef? = null,

    val metrics: Map<String, Double>? = null
)