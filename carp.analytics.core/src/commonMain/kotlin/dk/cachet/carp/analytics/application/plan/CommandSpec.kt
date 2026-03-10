package dk.cachet.carp.analytics.application.plan

import kotlinx.serialization.Serializable

/**
 * Execute an external command as a structured, tokenized command.
 *
 * - [executable] must not be blank.
 * - [args] are already tokenized (no shell parsing / quoting done by runners).
 */
@Serializable
data class CommandSpec(
    val executable: String,
    val args: List<ExpandedArg> = emptyList(),
) : TasksRun
{
    init
    {
        require(executable.isNotBlank())
        {
            "CommandRun.executable must not be blank."
        }
    }
}
