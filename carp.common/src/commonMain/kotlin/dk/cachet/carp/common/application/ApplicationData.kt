package dk.cachet.carp.common.application

import dk.cachet.carp.common.infrastructure.serialization.ApplicationDataSerializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport


/**
 * Holds extra [data] which is specific to concrete applications, sensors, or infrastructure, and isn't statically
 * known to the base infrastructure.
 *
 * While the [data] can be formatted in any way, when JSON serialization is applied and [data] contains a JSON element,
 * the data will be formatted as JSON (without escaping special characters). If the JSON contained in the string is
 * malformed, it will be serialized as a normal, escaped string.
 */
@Serializable( ApplicationDataSerializer::class )
@JsExport
data class ApplicationData( val data: String )
