package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds


@JsModule( "big.js" )
@JsNonModule
@Suppress( "FunctionName" )
external fun Big( number: Number ): dynamic


@Suppress( "UNUSED_VARIABLE" )
@JsExport
actual fun SyncPoint.applyToTimestamp( timestamp: Long ): Long
{
    val bigClock = Big( relativeClockSpeed )
    val bigOffset = Big( timestamp - sensorTimestampAtSyncPoint )

    val excludingEpoch = bigClock.times( bigOffset ).toFixed() as String
    return excludingEpoch.toLong() + synchronizedOn.toEpochMicroseconds()
}
