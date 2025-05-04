package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.devices.DeviceConfiguration
import kotlin.js.JsExport
import kotlin.js.JsName


/**
 * A helper class to construct iterable objects which list all available tasks for a [DeviceConfiguration].
 * All devices support [BackgroundTask], which is added by default as [BACKGROUND].
 *
 * Extend from this class as an object and assign members as follows: `val SOME_TASK = add { SomeTaskBuilder() }`.
 */
@Suppress( "NON_EXPORTABLE_TYPE" )
@JsExport
open class TaskConfigurationList private constructor(
    private val list: MutableList<SupportedTaskConfiguration<*, *, *>>
) : List<SupportedTaskConfiguration<*, *, *>> by list
{
    @JsName( "create" )
    constructor() : this( mutableListOf() )

    /**
     * All containing measures and/or outputs start running in the background once triggered.
     * The task runs for a specified duration, or until stopped, or until all measures and/or outputs have completed.
     */
    @Suppress("PropertyName", "VariableNaming" ) // This is only extended by object classes, making it a constant.
    val BACKGROUND = add { BackgroundTaskBuilder() }


    protected fun <
        TConfiguration : TaskConfiguration<TData>,
        TData : Data,
        TBuilder : TaskConfigurationBuilder<TConfiguration>
    > add( builder: () -> TBuilder ): SupportedTaskConfiguration<TConfiguration, TData, TBuilder> =
        SupportedTaskConfiguration( builder ).also { list.add( it ) }
}


/**
 * A [TaskConfiguration] which is listed as a supported task on a [DeviceConfiguration].
 */
@JsExport
class SupportedTaskConfiguration<
    TConfiguration : TaskConfiguration<TData>,
    TData : Data,
    TBuilder : TaskConfigurationBuilder<TConfiguration>
>( private val createBuilder: () -> TBuilder )
{
    /**
     * Create a [TaskConfiguration] supported on this device with [name] which uniquely defines the task.
     */
    fun create( name: String, builder: TBuilder.() -> Unit ): TConfiguration =
        createBuilder()
            .apply( builder )
            .build( name )
}
