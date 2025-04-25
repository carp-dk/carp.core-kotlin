package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.environment.Environment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
/**
 * Represents the execution context for a process, including runtime configuration,
 * dependencies, and broader execution parameters.
 *
 * @param environment Optional runtime environment configuration (e.g., CondaEnvironment).
 * @param envVariables Map of environment variables for the execution context.
 */

@Serializable
@SerialName("ExecutionContext")
data class ExecutionContext(
    val environment: Environment? = null,
    val envVariables: Map<String, String> = emptyMap(),
)