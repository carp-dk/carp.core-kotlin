@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.data.application

import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Instant


/**
 * Information about a sensor clock at [synchronizedOn] on a primary device
 * which allows converting sensor time to number of microseconds since the Unix epoch.
 *
 * The required units/sign to carry out this conversion are determined by the formula:
 * syncedTime = [relativeClockSpeed] * (sensorTime - [sensorTimestampAtSyncPoint]) + [synchronizedOn]
 */
@Serializable
@JsExport
data class SyncPoint(
    /**
     * The UTC time as measured on the primary device when it determined the synchronization point.
     */
    val synchronizedOn: Instant,
    /**
     * The sensor time at [synchronizedOn].
     */
    @Required
    val sensorTimestampAtSyncPoint: Long,
    /**
     * The relative clock speed of UTC time compared to the sensor clock,
     * calculated as the variation of UTC time divided by the variation of sensor time.
     *
     * E.g., if the sensor clock runs half as fast as UTC time, the relative clock speed is 2.
     */
    @Required
    val relativeClockSpeed: Double = 1.0
)
{
    companion object
    {
        /**
         * The default [SyncPoint] for sensors which return timestamps as number of microseconds since the Unix epoch.
         * Applying this [SyncPoint] to timestamps is a no-op.
         */
        val UnixEpoch: SyncPoint = SyncPoint( Instant.fromEpochSeconds( 0 ), 0 )
    }
}


/**
 * Convert [timestamp] obtained by the sensor clock this [SyncPoint] relates to
 * into number of microseconds since the Unix epoch.
 *
 * This requires a platform-specific implementation in order not to lose any precision; big decimal needs to be used.
 */
expect fun SyncPoint.applyToTimestamp( timestamp: Long ): Long
