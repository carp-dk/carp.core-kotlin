package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Declarative author-time definition of an R script execution.
 *
 * Models **what** to run and **how arguments are structured** — without any runtime behaviour.
 * The planner is responsible for compiling this into a [dk.cachet.carp.analytics.application.plan.CommandSpec]
 * (or equivalent) using the resolved R environment.
 *
 * Responsibilities of this class:
 * - Identify the R code to run via [entryPoint] ([RScript]).
 * - Carry a structured [args] list using the shared [dk.cachet.carp.analytics.domain.tasks.ArgToken] DSL.
 *
 */
@Serializable
@SerialName("RTaskDefinition")
data class RTaskDefinition(
    override val id: UUID,
    override val name: String,
    override val description: String? = null,

    /**
     * The R code to execute — an R script file path.
     *
     * @see RScript
     */
    val entryPoint: RScript,

    /**
     * A structured list of argument tokens that will be appended to the R invocation.
     *
     * These tokens are expanded at plan-time using resolved input/output bindings.
     * Defaults to an empty list when no additional arguments are required.
     */
    val args: List<ArgToken> = emptyList(),

    ) : TaskDefinition
{
    init
    {
        require(name.isNotBlank()) { "RTaskDefinition name must not be blank" }
    }
}

