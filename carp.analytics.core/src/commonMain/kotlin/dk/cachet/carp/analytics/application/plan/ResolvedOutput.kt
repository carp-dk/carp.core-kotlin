package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.FileLocation
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import kotlinx.serialization.Serializable


/**
 * Resolved output after planning phase.
 *
 * @property spec The output data specification (unchanged from authored step)
 * @property location The fully resolved DataLocation with concrete path
 */
@Serializable
data class ResolvedOutput(
    val spec: OutputDataSpec,
    val location: DataLocation
)
{
    /**
     * Get the file path from this resolved output.
     *
     * @return The path if location is FileLocation, null otherwise
     */
    fun getPath(): String? = (location as? FileLocation)?.path
}
