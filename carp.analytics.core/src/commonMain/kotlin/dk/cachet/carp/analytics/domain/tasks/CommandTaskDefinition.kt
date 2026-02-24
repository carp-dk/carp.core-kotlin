package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a command-line task definition in author-time model.
 *
 * This task describes how to execute an external process in a structured,
 * typed way — without relying on raw string interpolation.
 *
 * The command is represented as an executable with structured argument tokens
 * that are expanded at plan-time using resolved bindings.
 */
@Serializable
@SerialName("CommandTaskDefinition")
data class CommandTaskDefinition(
    override val id: UUID,
    override val name: String,
    override val description: String? = null,

    /**
     * The base executable to invoke (e.g., "cp", "python", "conda").
     */
    val executable: String,

    /**
     * A structured list of argument tokens representing the command invocation.
     * These tokens are expanded at plan-time using resolved bindings.
     */
    val args: List<ArgToken> = emptyList()
) : TaskDefinition
{

    init
    {
        require(executable.isNotBlank()) { "CommandTaskDefinition executable must not be blank" }
        require(name.isNotBlank()) { "CommandTaskDefinition name must not be blank" }
    }
}
