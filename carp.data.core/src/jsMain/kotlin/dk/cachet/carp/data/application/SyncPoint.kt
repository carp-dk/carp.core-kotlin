

package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds


@JsModule( "big.js" )
@JsNonModule
@Suppress( "FunctionName" )
external fun Big( number: Number ): dynamic


@Suppress( "UNUSED_VARIABLE" )
actual fun SyncPoint.applyToTimestamp( timestamp: Long ): Long
{
    val bigClock = Big( relativeClockSpeed )
    val bigOffset = Big( timestamp - sensorTimestampAtSyncPoint )

    // `js` needs to be used here to prevent `.times()` from being transpiled into a `*` operator:
    // https://youtrack.jetbrains.com/issue/KT-77320/Kotlin-JS-compiles-Big.js-times-method-to-operator
    val excludingEpoch = js( "bigClock.times( bigOffset )" ).toFixed() as String

    return excludingEpoch.toLong() + synchronizedOn.toEpochMicroseconds()
}
