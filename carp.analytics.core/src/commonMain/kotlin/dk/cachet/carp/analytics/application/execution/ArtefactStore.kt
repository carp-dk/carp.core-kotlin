package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Optional metadata about a produced artefact.
 */
@Serializable
data class ArtefactMetadata(
    val sizeBytes: Long? = null,
    val sha256: String? = null,
    val contentType: String? = null
)

interface ArtefactStore
{
    /**
     * Record an artefact produced by a step.
     */
    fun recordArtefact(
        stepId: UUID,
        outputId: UUID,
        location: ResourceRef,
        metadata: ArtefactMetadata = ArtefactMetadata()
    ): ProducedOutputRef?

    /**
     * Retrieve artefact by output ID.
     */
    fun getArtefact( outputId: UUID ): ProducedOutputRef?

    /**
     * Get all artefacts from a step.
     */
    fun getArtefactsByStep( stepId: UUID ): List<ProducedOutputRef>

    /**
     * Get all artefacts.
     */
    fun getAllArtefacts(): List<ProducedOutputRef>

    /**
     * Resolve full path to artefact (prepend workspace root for relative paths).
     */
    fun resolvePath( outputId: UUID ): String?
}

/**
 * Internal record with timestamp.
 */
data class ArtefactRecord(
    val stepId: UUID,
    val outputId: UUID,
    val producedOutputRef: ProducedOutputRef,
    val recordedAt: Instant
)
