package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable


/**
 * The lifecycle state of a run, or of a step within a run.
 *
 * A run ends in [SUCCEEDED], [FAILED] or [CANCELLED]. [SKIPPED] only applies to steps
 * that did not start because the run ended first.
 */
@Serializable
enum class ExecutionStatus
{
    PENDING,
    PROVISIONING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    SKIPPED
}
