package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Declarative author-time definition of a Python script or module execution.
 *
 * Models **what** to run and **how arguments are structured** — without any runtime behaviour.
 * The planner is responsible for compiling this into a [dk.cachet.carp.analytics.application.plan.CommandSpec]
 * (or equivalent) using the resolved Python environment.
 *
 * Responsibilities of this class:
 * - Identify the Python code to run via [entryPoint] ([Script] or [Module]).
 * - Carry a structured [args] list using the shared [ArgToken] DSL.
 * - Optionally declare a [workingDirectory] hint for path-relative scripts.
 *
 * Explicitly **not** the responsibility of this class:
 * - Resolving a Python executable or conda/venv environment.
 * - Performing filesystem existence checks.
 * - Spawning processes or activating environments.
 * - Any execution or side-effect logic.
 */
@Serializable
@SerialName("PythonTaskDefinition")
data class PythonTaskDefinition(
    override val id: UUID,
    override val name: String,
    override val description: String? = null,

    /**
     * The Python code to execute — either a script file path or a module name.
     *
     * @see Script
     * @see Module
     */
    val entryPoint: PythonEntryPoint,

    /**
     * A structured list of argument tokens that will be appended to the Python invocation.
     *
     * These tokens are expanded at plan-time using resolved input/output bindings.
     * Defaults to an empty list when no additional arguments are required.
     */
    val args: List<ArgToken> = emptyList(),

) : TaskDefinition
{
    init
    {
        require(name.isNotBlank()) { "PythonTaskDefinition name must not be blank" }

    }
}

