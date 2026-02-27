package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

/**
 * Enum representing the current lifecycle status of a workflow execution.
 *
 * These states are used to track progress and finality of executions in the system.
 */
@Serializable
enum class ExecutionStatus
{
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    SKIPPED
}