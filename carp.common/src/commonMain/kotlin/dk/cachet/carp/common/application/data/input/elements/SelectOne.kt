package dk.cachet.carp.common.application.data.input.elements

import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * User needs to select one out of multiple [options].
 */
@Serializable
@JsExport
data class SelectOne( override val prompt: String, val options: Set<String> ) : InputElement<String>
{
    init
    {
        require( options.isNotEmpty() ) { "At least one option needs to be specified." }
    }

    override fun isValid( input: String ): Boolean = input in options

    @JsExport.Ignore
    override fun getDataClass(): KClass<String> = String::class
}
