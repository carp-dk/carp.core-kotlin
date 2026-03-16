package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.plan.EnvironmentRef
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Tracks which environments exist and their metadata.
 */
interface EnvironmentRegistry
{

    /**
     * Register an environment as created.
     */
    fun register( environmentRef: EnvironmentRef, metadata: EnvironmentMetadata )

    /**
     * Check if an environment exists in the registry.
     */
    fun exists( environmentId: String ): Boolean

    /**
     * Get metadata for an environment.
     */
    fun getMetadata( environmentId: String ): EnvironmentMetadata?

    /**
     * List all registered environments.
     */
    fun list(): List<EnvironmentMetadata>

    /**
     * Remove an environment from the registry.
     */
    fun remove( environmentId: String )
}

/**
 * Runtime metadata for an environment.
 */
@Serializable
data class EnvironmentMetadata(
    val id: String, // run_id ± step_id + env_name
    val name: String, // Display name
    val kind: String, // conda, pixi, system, r
    val runId: String, // Run that created this
    val stepId: String? = null, // Step (optional, for isolation)
    val createdAt: Instant, // When created
    val lastUsedAt: Instant, // Last execution
    val sizeBytes: Long, // Disk space
    val reuseCount: Int = 0, // Reuse count
    val status: String = "active" // active, clean-up, purged
)
