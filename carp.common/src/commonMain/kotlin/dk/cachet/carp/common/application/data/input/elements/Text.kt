package dk.cachet.carp.common.application.data.input.elements

import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.reflect.KClass


/**
 * Text entry by the user.
 */
@Serializable
@JsExport
data class Text( override val prompt: String ) : InputElement<String>
{
    override fun isValid( input: String ): Boolean = true

    @JsExport.Ignore
    override fun getDataClass(): KClass<String> = String::class
}
