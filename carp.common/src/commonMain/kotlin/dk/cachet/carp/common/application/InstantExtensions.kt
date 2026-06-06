package dk.cachet.carp.common.application

import kotlinx.datetime.Instant
import kotlin.js.JsExport


private const val MICROS_PER_SECOND = 1_000_000
private const val NANOS_PER_MICRO = 1_000


/**
 * Get the elapsed microseconds since the start of the UTC day 1970-01-01 at this [Instant]'s moment in time.
 */
@JsExport
fun Instant.toEpochMicroseconds(): Long = epochSeconds * MICROS_PER_SECOND + nanosecondsOfSecond / NANOS_PER_MICRO
