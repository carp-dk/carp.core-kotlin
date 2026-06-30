package dk.cachet.carp.common.application.users

import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Determines which participant roles to assign to something.
 */
@Serializable
@JsExport
sealed class AssignedTo
{
    /**
     * Assign this to all participants in the study protocol.
     */
    @Serializable
    object All : AssignedTo()

    /**
     * Assign this to the specified [roleNames] in the study protocol.
     */
    @Serializable
    data class Roles( val roleNames: Set<String> ) : AssignedTo()
}
