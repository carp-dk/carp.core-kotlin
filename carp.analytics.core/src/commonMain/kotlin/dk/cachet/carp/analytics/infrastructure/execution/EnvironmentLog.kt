package dk.cachet.carp.analytics.infrastructure.execution


import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Log entry for environment operations.
 */
@Serializable
data class EnvironmentLog(
    val timestamp: Instant,
    val environmentId: String,
    val phase: EnvironmentPhase,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class EnvironmentPhase
{
    SETUP, // Environment creation
    EXECUTION, // Running steps
    TEARDOWN // Cleanup
}

enum class LogLevel
{
    INFO, WARN, ERROR
}

/**
 * Container for environment execution logs.
 */
@Serializable
data class EnvironmentExecutionLogs(
    val setupLogs: List<EnvironmentLog> = emptyList(),
    val executionLogs: List<EnvironmentLog> = emptyList(),
    val teardownLogs: List<EnvironmentLog> = emptyList()
)
