package dk.cachet.carp.analytics.domain.trigger

import kotlinx.datetime.*
import kotlin.test.*

class CronExpressionTest
{
    private val zone = TimeZone.UTC

    @Test
    fun throws_on_blank_expression()
    {
        assertFailsWith<IllegalArgumentException>
        {
            CronExpression("")
        }
    }

    @Test
    fun throws_on_invalid_expression()
    {
        assertFailsWith<IllegalArgumentException>
        {
            CronExpression("invalid cron")
        }
    }

    @Test
    fun getNextScheduledTime_returns_next_time_after_given_instant()
    {
        val cron = CronExpression("* * * ? * * *") // every hour at minute 0
        val now = LocalDateTime(2025, 6, 18, 10, 30).toInstant(zone)
        val next = cron.getNextScheduledTime(now)

        assertNotNull(next)
        assertTrue(next > now.toLocalDateTime(zone))
    }

    @Test
    fun shouldTriggerAt_returns_true_when_due()
    {
        val cron = CronExpression("0 0 10 ? * * *") // every day at 10:00
        val now = LocalDateTime(2025, 6, 18, 10, 0).toInstant(zone)
        val result = cron.shouldTriggerAt(now, null)

        assertTrue(result)
    }

    @Test
    fun shouldTriggerAt_returns_false_when_not_due()
    {
        val cron = CronExpression("0 0 11 ? * * *") // every day at 11:00
        val now = LocalDateTime(2025, 6, 18, 10, 0).toInstant(zone)
        val result = cron.shouldTriggerAt(now, null)

        assertFalse(result)
    }

    @Test
    fun shouldTriggerAt_returns_true_if_next_run_after_lastFired_is_now_or_before()
    {
        val cron = CronExpression("0 * * ? * * *") // every hour at minute 0
        val lastFired = LocalDateTime(2025, 6, 18, 8, 0).toInstant(zone)
        val now = LocalDateTime(2025, 6, 18, 9, 0).toInstant(zone)

        assertTrue(cron.shouldTriggerAt(now, lastFired))
    }

    @Test
    fun shouldTriggerAt_returns_false_if_next_run_after_lastFired_is_later_than_now()
    {
        val cron = CronExpression("0 0 * ? * * *") // every hour at minute 0
        val lastFired = LocalDateTime(2025, 6, 18, 8, 30).toInstant(zone)
        val now = LocalDateTime(2025, 6, 18, 8, 45).toInstant(zone)

        assertFalse(cron.shouldTriggerAt(now, lastFired))
    }
}
