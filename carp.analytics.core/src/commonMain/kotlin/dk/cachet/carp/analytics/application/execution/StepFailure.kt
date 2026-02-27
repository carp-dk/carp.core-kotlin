package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

@Serializable
data class StepFailure(
    val kind: FailureKind,
    val message: String
)

@Serializable
enum class FailureKind
{
    COMMAND_FAILED,
    TIMEOUT,
    CANCELLED,
    INFRASTRUCTURE,
    UNKNOWN
}
