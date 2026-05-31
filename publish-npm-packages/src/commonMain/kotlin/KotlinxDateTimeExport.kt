@file:Suppress(
    "NON_EXPORTABLE_TYPE",
    "UNUSED_VARIABLE" // The variable names show up in generated JS sources which is useful to look up mangled names.
)

import kotlin.js.JsExport
import kotlin.time.Clock
import kotlin.time.Instant


/**
 * Refers to types/methods in the kotlinx datetime library to ensure they aren't removed from compiled sources
 * as part of the JS IR backend's compiler optimizations.
 * The exported JS sources for this class can also be used to look up mangled method names.
 */
@JsExport
class KotlinxDateTimeExport
{
    fun clock( clock: Clock )
    {
        val system: Clock = Clock.System
        val now = clock.now()
    }

    fun instant( instant: Instant )
    {
        val toEpochMilliseconds = instant.toEpochMilliseconds()
    }
}
