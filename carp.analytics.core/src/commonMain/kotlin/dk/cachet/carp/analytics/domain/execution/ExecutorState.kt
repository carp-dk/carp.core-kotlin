package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ExecutorState(
    val executionId: UUID,
    val status: ExecutionStatus,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val workflowId: UUID,
    val studyId: UUID
)
