package dk.cachet.carp.analytics.domain.trigger

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * A representation of a cron expression, used to schedule repeating triggers.
 * This is designed to be backend evaluated, not device-local.
 */
@Serializable
@SerialName("CronExpression")
data class CronExpression(
    val expression: String
) {
    init {
        require(expression.isNotBlank()) { "Cron expression cannot be blank." }
        // TODO: Validate the cron expression format.
    }

    override fun toString(): String = expression
}
