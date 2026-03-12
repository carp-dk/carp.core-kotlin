package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Clock

/**
 * In-memory implementation of ArtefactStore.
 * Used for testing and simple scenarios.
 *
 * Does not persist to disk.
 */
class InMemoryArtefactStore(
    private val clock: Clock = Clock.System
) : ArtefactStore {

    private val _registry = mutableMapOf<UUID, ArtefactRecord>()


    override fun recordArtefact(
        stepId: UUID,
        outputId: UUID,
        location: ResourceRef,
        metadata: ArtefactMetadata
    ): ProducedOutputRef? {
        return try {
            val producedRef = ProducedOutputRef(
                outputId = outputId,
                location = location,
                sizeBytes = metadata.sizeBytes,
                sha256 = metadata.sha256,
                contentType = metadata.contentType
            )

            val record = ArtefactRecord(
                stepId = stepId,
                outputId = outputId,
                producedOutputRef = producedRef,
                recordedAt = clock.now()
            )

            _registry[outputId] = record
            producedRef
        } catch (_: Exception) {
            null
        }
    }

    override fun getArtefact(outputId: UUID): ProducedOutputRef? =
        _registry[outputId]?.producedOutputRef

    override fun getArtefactsByStep(stepId: UUID): List<ProducedOutputRef> =
        _registry.values
            .filter { it.stepId == stepId }
            .map { it.producedOutputRef }

    override fun getAllArtefacts(): List<ProducedOutputRef> =
        _registry.values.map { it.producedOutputRef }

    override fun resolvePath(outputId: UUID): String? =
        getArtefact(outputId)?.location?.value
}