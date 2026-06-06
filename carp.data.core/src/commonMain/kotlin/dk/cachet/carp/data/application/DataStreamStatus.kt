package dk.cachet.carp.data.application

import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Status of a [dataStream] in a study deployment.
 */
@Serializable
@JsExport
data class DataStreamStatus(
    val dataStream: DataStreamId,
    /**
     * The sequence number of the last received measurement for this [dataStream],
     * or `null` when no data has been received yet.
     */
    val lastSequenceId: Long?,
    /**
     * Determines whether this [dataStream] currently accepts incoming data.
     */
    val isOpen: Boolean
)
