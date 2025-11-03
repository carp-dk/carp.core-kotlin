package dk.cachet.carp.analytics.domain.trigger

import com.ucasoft.kcron.Cron
import com.ucasoft.kcron.abstractions.CronDateTimeProvider
import com.ucasoft.kcron.core.exceptions.WrongCronExpression
import com.ucasoft.kcron.kotlinx.datetime.CronLocalDateTime
import com.ucasoft.kcron.kotlinx.datetime.toCronLocalDateTime
import kotlinx.datetime.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.ucasoft.kcron.core.Cron as CronCore


/**
 * A representation of a cron expression, used to schedule repeating triggers.
 * This is designed to be backend evaluated, not device-local.
 */
@Serializable
@SerialName("CronExpression")
data class CronExpression(
    val expression: String
)
{
    init
    {
        require(expression.isNotBlank()) { "Cron expression cannot be blank." }
        try
        {
            Cron.parseAndBuild(expression)
        }
        catch ( e: WrongCronExpression )
        {
            throw IllegalArgumentException("Invalid cron expression: $expression", e)
        }
    }

    override fun toString(): String = expression

    /**
     * Get the next scheduled time from the provided [Instant], or `null` if no future match exists.
     */
    fun getNextScheduledTime( from: Instant ): LocalDateTime?
    {
        val fromLocal = from.toLocalDateTime(TimeZone.UTC)
        val provider = FixedCronDateTimeProvider(fromLocal)

        val cron = CronCore.parseAndBuild(expression, provider)
        return cron.nextRun
    }

    /**
     * Returns `true` if this cron should trigger at the given [now] instant, based on a [lastFired] time.
     */
    fun shouldTriggerAt( now: Instant, lastFired: Instant? ): Boolean
    {
        val next = getNextScheduledTime(lastFired ?: now.minus(1, DateTimeUnit.SECOND))
        return next != null && next <= now.toLocalDateTime(TimeZone.UTC)
    }
}


@Suppress("Immutable", "DataClass")
class FixedCronDateTimeProvider(
    private val fixedNow: LocalDateTime
) : CronDateTimeProvider<LocalDateTime, CronLocalDateTime>
{

    override fun now(): CronLocalDateTime = fixedNow.toCronLocalDateTime()

    override fun from(
        year: Int,
        month: Int,
        day: Int,
        hours: Int,
        minutes: Int,
        seconds: Int
    ): CronLocalDateTime =
        CronLocalDateTime( year, month, day, hours, minutes, seconds )

    override fun from( original: LocalDateTime ): CronLocalDateTime =
        original.toCronLocalDateTime()
}
