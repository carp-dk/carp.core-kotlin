package dk.cachet.carp.studies.application.users

import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * The representation of a participant group.
 */
@Serializable
@JsExport
data class ParticipantGroupRepresentation(
    /**
     * The name of the participant group.
     *
     * When `null`, no name is set for the participant group and a display name may instead be derived from the
     * participants in the group.
     */
    val name: String?
)
{
    companion object
    {
        /**
         * The default representation for participant groups without an explicit name.
         */
        val Default: ParticipantGroupRepresentation = ParticipantGroupRepresentation( null )
    }
}
