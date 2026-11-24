package dk.cachet.carp.analytics.domain.workflow

import kotlinx.serialization.Serializable


/**
 * A step library step pinned to an exact version.
 *
 * @property uses The step reference as written in the workflow.
 * @property contentHash Hash of the step's content, used to check that the pinned step has not changed.
 */
@Serializable
data class LockedStep(
    val uses: String,
    val version: String,
    val contentHash: String
)


/**
 * The step library versions a workflow version uses.
 */
@Serializable
data class StepsLock( val steps: List<LockedStep> = emptyList() )
{
    /**
     * Returns the pinned entry for [uses], or null when the workflow does not use it.
     */
    fun entryFor( uses: String ): LockedStep? = steps.firstOrNull { it.uses == uses }
}
