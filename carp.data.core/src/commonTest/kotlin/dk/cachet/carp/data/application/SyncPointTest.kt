package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.toEpochMicroseconds
import kotlin.test.*
import kotlin.time.Clock


/**
 * Tests for [SyncPoint].
 */
class SyncPointTest
{
    @Test
    fun applyToTimestamp_for_same_clock_speed_succeeds()
    {
        val now = Clock.System.now()
        val sync = SyncPoint( now, 1 )

        val synchronized = sync.applyToTimestamp( 1 )
        assertEquals( now.toEpochMicroseconds(), synchronized )
    }

    @Test
    fun applyToTimestamp_for_double_speed_clock_succeeds()
    {
        val doubleSpeed = createDoubleSpeedSyncPoint()

        val synchronized = doubleSpeed.applyToTimestamp( 1000 )
        assertEquals( 500, synchronized )
    }

    @Test
    fun applyToTimestamp_for_unix_epoch_syncpoint_succeeds()
    {
        val synchronized = SyncPoint.UnixEpoch.applyToTimestamp( 1000 )
        assertEquals( 1000, synchronized )
    }

    @Test
    fun applyToTimestamp_has_accurate_precision()
    {
        val bigNumber = 290017789727876000L

        val noOp = SyncPoint.UnixEpoch.applyToTimestamp( bigNumber )
        assertEquals( bigNumber, noOp )
    }
}
