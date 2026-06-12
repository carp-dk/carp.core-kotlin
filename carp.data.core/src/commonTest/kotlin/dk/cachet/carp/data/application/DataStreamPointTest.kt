package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.toEpochMicroseconds
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.infrastructure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock


/**
 * Tests for [DataStreamPoint].
 */
class DataStreamPointTest
{
    @Test
    fun initializing_DataStreamPoint_with_negative_sequenceId_fails()
    {
        val now = Clock.System.now()
        assertFailsWith<IllegalArgumentException> {
            DataStreamPoint(
                -1,
                UUID.randomUUID(),
                "Device",
                measurement( StubDataPoint(), 0 ),
                listOf( 1 ),
                SyncPoint( now, now.toEpochMicroseconds() )
            )
        }
    }

    @Test
    fun initializing_DataStreamPoint_with_empty_triggerIds_fails()
    {
        val now = Clock.System.now()
        assertFailsWith<IllegalArgumentException> {
            DataStreamPoint(
                0,
                UUID.randomUUID(),
                "Device",
                measurement( StubDataPoint(), 0 ),
                emptyList(),
                SyncPoint( now, now.toEpochMicroseconds() )
            )
        }
    }

    @Test
    fun synchronize_succeeds()
    {
        val doubleSpeed = createDoubleSpeedSyncPoint()
        val point = DataStreamPoint(
            0,
            UUID.randomUUID(),
            "Test device",
            measurement( StubDataPoint(), 1000, null ),
            listOf( 0 ),
            doubleSpeed
        )

        val synchronized = point.synchronize()
        assertEquals( 500, synchronized.measurement.sensorStartTime )
        assertEquals( SyncPoint.UnixEpoch, synchronized.syncPoint )
    }
}
