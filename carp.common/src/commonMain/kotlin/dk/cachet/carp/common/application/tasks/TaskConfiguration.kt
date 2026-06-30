package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.Immutable
import dk.cachet.carp.common.application.ImplementAsDataClass
import dk.cachet.carp.common.application.data.CarpDataTypes
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Describes requested measures and/or output to be presented on a device.
 * TODO: Outputs are not yet specified.
 */
@Polymorphic
@Immutable
@ImplementAsDataClass
@JsExport
interface TaskConfiguration<TData : Data>
{
    /**
     * A name which uniquely identifies the task.
     */
    val name: String

    /**
     * The data which needs to be collected/measured passively as part of this task.
     */
    val measures: List<Measure>

    /**
     * A description of this task, emphasizing the reason why the data is collected.
     */
    val description: String?
}


/**
 * Get data types of all data which may be collected, either passively as part of task measures,
 * or as the result of user interactions, for this task.
 */
@JsExport
fun TaskConfiguration<*>.getAllExpectedDataTypes(): Set<DataType> =
    measures.map { measure ->
        when ( measure )
        {
            is Measure.TriggerData -> CarpDataTypes.TRIGGERED_TASK.type
            is Measure.DataStream -> measure.type
        }
    }
    .plus( CarpDataTypes.COMPLETED_TASK.type )
    .toSet()


/**
 * A helper class to configure and construct immutable [TaskConfiguration] classes.
 */
@TaskConfigurationBuilderDsl
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
abstract class TaskConfigurationBuilder<TConfiguration : TaskConfiguration<*>>
{
    /**
     * The data which needs to be collected/measures as part of this task.
     */
    var measures: List<Measure> = emptyList()

    /**
     * A description of this task, emphasizing the reason why the data is collected.
     */
    var description: String? = null

    abstract fun build( name: String ): TConfiguration
}

/**
 * Should be applied to all builders participating in building [TaskConfiguration]s to prevent misuse of internal DSL.
 * For more information: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html
 */
@DslMarker
annotation class TaskConfigurationBuilderDsl
