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
    private val int = 42
    val toLong = int.toLong() // Needs to be loaded from field to be exported.

    fun long( long: Long )
    {
        val toNumber = long.toInt()
    }

    fun collection( collection: Collection<Any> )
    {
        val contains = collection.contains( 42 )
        val size = collection.size
    }

    // Two values needed to ensure export which takes an array and unpacks it.
    val listOf = listOf( 42, 42 )
    val setOf = setOf( 42, 42 )

    fun map( map: Map<Any, Any> )
    {
        val get = map[ 42 ]
        val keys = map.keys
        val values = map.values
    }

    fun pair( pair: Pair<Any, Any> )
    {
        val to = 42 to "answer"
        val first = pair.first
        val second = pair.second
    }

    val mapOf = mapOf( 42 to "answer", 13 to "unlucky" )

    fun duration( duration: Duration )
    {
        val parseIsoString = Duration.parseIsoString("PT1M")
        val companion = Duration.Companion
        val zero = Duration.ZERO
        val infinite = Duration.INFINITE
        val inWholeMilliseconds = duration.inWholeMilliseconds
        val inWholeMicroseconds = duration.inWholeMicroseconds
    }
}
