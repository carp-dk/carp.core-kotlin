package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * The result of executing a single planned step.
 *
 * @param stepId      Matches [dk.cachet.carp.analytics.application.plan.PlannedStep.stepId].
 * @param status      Status of this step.
 * @param startedAt   Wall-clock time when the step began execution; null if never started.
 * @param finishedAt  Wall-clock time when the step completed; null if it never finished.
 * @param outputs     Declared output artefacts, each with a workspace-relative location.
 * @param failure     Non-null when [status] is [ExecutionStatus.FAILED].
 * @param detail      Optional low-level diagnostics (command, exit code, log refs).
 */
@Serializable
data class StepRunResult(
    val stepId: UUID,
    val status: ExecutionStatus,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val outputs: List<ProducedOutputRef>? = emptyList(),
    val failure: StepFailure? = null,
    val detail: StepRunDetail? = null
)
