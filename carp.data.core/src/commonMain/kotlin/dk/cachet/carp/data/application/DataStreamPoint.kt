@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A [measurement] as part of a [dataStream] defined in an associated deployed study protocol
 * which was collected because the triggers identified by [triggerIds] requested data collection.
 */
@Serializable
@JsExport
data class DataStreamPoint<out TData : Data>(
    /**
     * The sequence number of this [measurement] in the data stream, acting as a unique key.
     * Sequence numbers are assigned by the primary device which coordinates the data collection,
     * starting from zero and incremented by 1 for each new measurement.
     */
    val sequenceId: Long,
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    @Serializable( MeasurementSerializer::class )
    val measurement: Measurement<TData>,
    val triggerIds: List<Int>,
    /**
     * The most recent synchronization information which was determined for this or a previous [DataStreamPoint].
     */
    val syncPoint: SyncPoint = SyncPoint.UnixEpoch
)
{
    init
    {
        require( sequenceId >= 0 ) { "Sequence ID must be positive." }
        require( triggerIds.isNotEmpty() )
            { "Data always needs to be linked to at least one trigger that requested it." }
    }

    val dataStream: DataStreamId
        get() = DataStreamId( studyDeploymentId, deviceRoleName, measurement.dataType )

    /**
     * Convert this [DataStreamPoint] to one for which the [measurement] is synchronized using [syncPoint].
     */
    fun synchronize(): DataStreamPoint<TData>
    {
        // Early out in case no synchronization is needed.
        if ( syncPoint == SyncPoint.UnixEpoch ) return this

        return copy(
            measurement = measurement.synchronize( syncPoint ),
            syncPoint = SyncPoint.UnixEpoch
        )
    }
}
