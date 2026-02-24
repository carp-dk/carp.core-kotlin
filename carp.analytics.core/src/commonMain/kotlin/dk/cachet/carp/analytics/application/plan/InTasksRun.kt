package dk.cachet.carp.analytics.application.plan

import kotlinx.serialization.Serializable

/**
 * Execute an operation *inside* the CARP-DSP host process (i.e., within the framework runtime),
 * as opposed to spawning an external OS process.
 *
 * This is intentionally platform-agnostic
 *
 * The operation itself is identified by [operationId]. Serializable [parameters] may be provided.
 */
@Serializable
data class InTasksRun(
    val operationId: String,
    val parameters: Map<String, String> = emptyMap(),
) : TasksRun
{
    val safeParameters: Map<String, String> = parameters.toMap()

    init
    {
        require(operationId.isNotBlank())
        {
            "InProcessRun.operationId must not be blank."
        }
    }
}
