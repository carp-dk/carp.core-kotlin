package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Runtime record of a produced output for a step execution.
 *
 *
 * Semantics:
 * - [location] MUST be workspace-root-relative (ResourceKind.RELATIVE_PATH)
 * - For step outputs, [location] MUST resolve under: steps/{stepMetadata}/outputs/
 *
 * Rich metadata such as schema/format belongs on OutputDataSpec (author/plan time),
 * not on this runtime record.
 */
@Serializable
data class ProducedOutputRef(
    val outputId: UUID,
    val location: ResourceRef,
    val sizeBytes: Long? = null,
    val sha256: String? = null,
    val contentType: String? = null
)
