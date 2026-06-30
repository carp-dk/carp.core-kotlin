package dk.cachet.carp.common.application.data.input.elements

import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * Describes data and its constraints which may be input by a user.
 */
@JsExport
interface InputElement<TData : Any>
{
    /**
     * The prompt to be shown to the user to indicate which data may be input.
     */
    val prompt: String

    /**
     * Validates whether the provided [input] matches the constraints of this input element.
     */
    fun isValid( input: TData ): Boolean

    /**
     * Return the class information of the expected input data ([TData]).
     */
    @JsExport.Ignore
    fun getDataClass(): KClass<TData>
}


typealias AnyInputElement = InputElement<*>
