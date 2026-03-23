package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PlanDiagnostics(
    val planId: String,
    val workflowId: String,
    val timestamp: String, // ISO-8601 timestamp

    // Counts
    val stepCount: Int,
    val environmentCount: Int,
    val bindingCount: Int,

    // Execution
    val executionOrder: List<String>, // Step IDs in order

    // Issues
    val issueSummary: IssueSummary,
    val issues: List<PlanIssue>,

    // Steps
    val stepSummaries: List<PlannedStepSummary>,

    // Hash
    val planHash: String,

    // Validity
    val isValid: Boolean
)

@Serializable
data class IssueSummary(
    val errorCount: Int,
    val warningCount: Int,
    val infoCount: Int
)

@Serializable
data class PlannedStepSummary(
    val metadata: StepMetadata,
    val environmentId: UUID?,
    val inputCount: Int,
    val outputCount: Int
)
