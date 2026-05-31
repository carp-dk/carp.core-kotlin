@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Contains detailed information about a study, such as the configured study protocol.
 */
@Serializable
@JsExport
data class StudyDetails(
    val studyId: UUID,
    /**
     * The ID of the entity (e.g., person or group) that created this study.
     */
    val ownerId: UUID,
    /**
     * A descriptive name for the study, assigned by, and only visible to, the study owner.
     */
    val name: String,
    /**
     * The date when this study was created.
     */
    val createdOn: Instant,
    /**
     * A description for the study, assigned by, and only visible to, the study owner.
     */
    val description: String?,
    /**
     * A description of the study, shared with participants once they are invited to the study.
     */
    val invitation: StudyInvitation,
    /**
     * A snapshot of the protocol to use in this study, or null when not yet defined.
     */
    val protocolSnapshot: StudyProtocolSnapshot?
)
