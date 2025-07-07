package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
    * A serializable snapshot of a [StudyProtocol] and its version, used in a study.
 */
@Serializable
@JsExport
data class VersionedStudyProtocolSnapshot (
    /**
     * A snapshot of the protocol to use in this study, or null when not yet defined.
     */
    val protocolSnapshot: StudyProtocolSnapshot,
    /**
     * The version of the protocol to use in this study, or null when not yet defined.
     */
    val protocolVersion: ProtocolVersion
)

