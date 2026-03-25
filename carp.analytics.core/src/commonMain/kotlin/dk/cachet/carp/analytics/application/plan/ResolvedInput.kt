package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.FileLocation
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import kotlinx.serialization.Serializable

/**
 * Resolved input after planning phase.
 *
 * @property spec The input data specification (unchanged from authored step)
 * @property location The fully resolved DataLocation with concrete path
 *   - For external inputs: path from InputDataSpec.location
 *   - For step inputs: path from producer's ResolvedOutput.location
 */
@Serializable
data class ResolvedInput(
    val spec: InputDataSpec,
    val location: DataLocation
)
{
    /**
     * Get the file path from this resolved input.
     *
     * @return The path if location is FileLocation, null otherwise
     */
    fun getPath(): String? = (location as? FileLocation)?.path
}
