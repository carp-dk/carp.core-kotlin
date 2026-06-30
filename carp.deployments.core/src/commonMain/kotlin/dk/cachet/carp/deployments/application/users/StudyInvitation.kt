package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.ApplicationData
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A description of a study, shared with participants once they are invited to a study.
 */
@Serializable
@JsExport
data class StudyInvitation(
    /**
     * A descriptive name for the study to be shown to participants.
     */
    val name: String,
    /**
     * A description of the study clarifying to participants what it is about.
     */
    val description: String? = null,
    /**
     * Application-specific data to be shared with clients when they are invited to a study.
     *
     * This can be used by infrastructures or concrete applications which require exchanging additional data
     * between the studies and clients subsystems, outside of scope or not yet supported by CARP core.
     */
    val applicationData: ApplicationData? = null
)
