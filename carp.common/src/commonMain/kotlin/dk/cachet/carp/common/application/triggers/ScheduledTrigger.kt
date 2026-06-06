package dk.cachet.carp.common.application.triggers

import dk.cachet.carp.common.application.RecurrenceRule
import dk.cachet.carp.common.application.TimeOfDay
import dk.cachet.carp.common.application.data.NoData
import dk.cachet.carp.common.application.devices.AnyPrimaryDeviceConfiguration
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.js.JsName


/**
 * A trigger which starts a task according to a recurring schedule starting on the date that the study starts.
 * The iCalendar RFC 5545 standard is used to specify the recurrence rule: https://tools.ietf.org/html/rfc5545#section-3.3.10
 * This trigger needs to be evaluated on a primary device since it is time bound and therefore requires a task scheduler.
 */
@Serializable
@JsExport
data class ScheduledTrigger private constructor(
    override val sourceDeviceRoleName: String,
    val time: TimeOfDay,
    val recurrenceRule: RecurrenceRule
) : TriggerConfiguration<NoData>()
{
    @Transient
    override val requiresPrimaryDevice: Boolean = true

    @JsName( "create" )
    @Suppress( "NON_EXPORTABLE_TYPE" )
    constructor(
        /**
         * The primary device on which this trigger is evaluated.
         */
        sourceDevice: AnyPrimaryDeviceConfiguration,
        /**
         * The local time of day when the [recurrenceRule] should be applied on the day that the study has started,
         * or the next day after in case this time has passed already.
         */
        time: TimeOfDay,
        /**
         * The recurring schedule at which to trigger the task.
         * According to RFC 5545, the start date time should align with the recurrence rule.
         * To calculate recurrences, use the first date and time matching the recurrence rule on or after the study starts.
         */
        recurrenceRule: RecurrenceRule
    ) : this( sourceDevice.roleName, time, recurrenceRule )
}
