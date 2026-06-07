@file:Suppress(
    "MagicNumber",
    "NON_EXPORTABLE_TYPE",
    "UNUSED_VARIABLE" // The variable names show up in generated JS sources which is useful to look up mangled names.
)

import kotlin.js.JsExport
import kotlin.time.Duration

/**
 * Refers to types/methods in the kotlin standard library to ensure they aren't removed from compiled sources
 * as part of the JS backend's compiler optimizations.
 * The exported JS sources for this class can also be used to look up mangled method names.
 */
@JsExport
class KotlinExport
{
    fun pair( pair: Pair<Any, Any> )
    {
        val to = 42 to "answer"
        val first = pair.first
        val second = pair.second
    }

    fun duration( duration: Duration )
    {
        val parseIsoString = Duration.parseIsoString("PT1M")
        val companion = Duration.Companion
        val zero = Duration.ZERO
        val infinite = Duration.INFINITE
        val inWholeMilliseconds = duration.inWholeMilliseconds
        val inWholeMicroseconds = duration.inWholeMicroseconds
        val toString = duration.toString()
    }
}
