package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID

/**
 * Base exception for all workflow execution errors.
 *
 * All exceptions in the execution layer are specific subtypes.
 * Never catch WorkflowExecutionException directly - catch specific types.
 */
sealed class WorkflowExecutionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a single step execution fails.
 *
 * Includes step ID for error tracking and recovery.
 */
class StepExecutionException(
    message: String,
    val stepId: UUID,
    val exitCode: Int? = null,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when input/output data cannot be resolved.
 *
 * Includes data reference ID for debugging.
 */
class DataResolutionException(
    message: String,
    val dataRefId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when environment setup fails.
 *
 * Includes environment ID for recovery.
 */
class EnvironmentSetupException(
    message: String,
    val envId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when step artefacts cannot be collected.
 *
 * Includes artifact ID for recovery.
 */
class ArtefactCollectionException(
    message: String,
    val artefactId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when data flow between steps is invalid.
 *
 * Includes source and target step IDs.
 */
class DataFlowException(
    message: String,
    val sourceStepId: UUID,
    val targetStepId: UUID,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when execution plan validation fails.
 *
 * Includes all validation issues.
 */
class WorkflowValidationException(
    message: String,
    val issues: List<String> = emptyList()
) : WorkflowExecutionException(message)

/**
 * Thrown when process execution fails (system-level).
 *
 * Includes command and exit information.
 */
class ProcessExecutionException(
    message: String,
    val command: String,
    val exitCode: Int? = null,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)

/**
 * Thrown when file I/O operations fail during execution.
 *
 * Includes file path for debugging.
 */
class ExecutionIOException(
    message: String,
    val filePath: String,
    cause: Throwable? = null
) : WorkflowExecutionException(message, cause)
