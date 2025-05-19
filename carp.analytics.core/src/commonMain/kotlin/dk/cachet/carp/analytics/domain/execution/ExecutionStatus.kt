package dk.cachet.carp.analytics.domain.execution

import kotlinx.serialization.Serializable

@Serializable
enum class ExecutionStatus {
    SUCCESS,
    FAILURE,
    PARTIAL,
    SKIPPED,
    RUNNING
}
