package dk.cachet.carp.analytics.application.exceptions

import dk.cachet.carp.common.application.UUID

/**
 * Base exception for all workflow-related errors.
 *
 * Covers errors that can occur during:
 * - Authoring (YAML → Descriptors → Domain Models)
 * - Planning (Validation → Deterministic Planning)
 * - Execution (Running Steps → ExecutionReport)
 *
 * This unified hierarchy allows:
 * - Catching all workflow errors: `catch (e: WorkflowException)`
 * - Catching phase-specific errors: `catch (e: WorkflowAuthoringException)`
 * - Clear error categorization and handling
 *
 * NOTE: Exceptions are not serialized. When including error information
 * in ExecutionReport, use the message string via ExecutionIssue.
 */
sealed class WorkflowException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// AUTHORING PHASE

sealed class WorkflowAuthoringException(
    message: String,
    cause: Throwable? = null
) : WorkflowException(message, cause)

class YamlCodecException(
    message: String,
    cause: Throwable? = null
) : WorkflowAuthoringException(message, cause)

class UnsupportedTaskTypeException(
    message: String,
    val typeName: String
) : WorkflowAuthoringException(message)

class UnsupportedEnvironmentKindException(
    message: String,
    val kind: String
) : WorkflowAuthoringException(message)

class InvalidWorkflowDefinitionException(
    message: String,
    val issues: List<String> = emptyList()
) : WorkflowAuthoringException(message)

// PLANNING PHASE

sealed class WorkflowPlanningException(
    message: String,
    cause: Throwable? = null
) : WorkflowException(message, cause)

class InvalidExecutionPlanException(
    message: String,
    val issues: List<String> = emptyList()
) : WorkflowPlanningException(message)


// EXECUTION PHASE

sealed class WorkflowExecutionException(
    message: String,
    cause: Throwable? = null
) : WorkflowException(message, cause)

class StepExecutionException(
    message: String,
    val stepId: UUID,
    val exitCode: Int? = null,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class ProcessExecutionException(
    message: String,
    val command: String,
    val exitCode: Int? = null,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class EnvironmentSetupException(
    message: String,
    val envId: String,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class ArtefactCollectionException(
    message: String,
    val artefactId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class DataResolutionException(
    message: String,
    val dataRefId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class DataFlowException(
    message: String,
    val sourceStepId: UUID,
    val targetStepId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

class WorkflowValidationException(
    message: String,
    val issues: List<String> = emptyList()
) : WorkflowExecutionException(message)

class ExecutionIOException(
    message: String,
    val filePath: String,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)
