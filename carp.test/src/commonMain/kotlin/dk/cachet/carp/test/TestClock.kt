package dk.cachet.carp.test

import kotlin.time.Clock
import kotlin.time.Instant


/**
 * A fixed clock at UNIX epoch for testing with deterministic behavior.
 */
object TestClock : Clock
{
    private val currentInstant: Instant = Instant.fromEpochSeconds( 0 )

    override fun now(): Instant = currentInstant
}
