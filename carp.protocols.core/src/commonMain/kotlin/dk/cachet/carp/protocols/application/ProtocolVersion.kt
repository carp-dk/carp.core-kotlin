package dk.cachet.carp.protocols.application

import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Specifies a specific version for a [StudyProtocol], identified by a [tag].
 *
 * @param date The date when this version of the protocol was created.
 */
@Suppress( "NON_EXPORTABLE_TYPE" )
@Serializable
@JsExport
data class ProtocolVersion(
    val tag: String,
    @Required
    val date: Instant = Clock.System.now()
)
