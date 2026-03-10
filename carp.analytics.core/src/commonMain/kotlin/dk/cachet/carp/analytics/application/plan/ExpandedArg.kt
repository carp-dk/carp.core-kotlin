package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

/**
 * Representation of an expanded argument token.
 *
 * After expansion, arguments are no longer just strings—they carry information
 * about what runtime resolution they need:
 *
 * - Literal: A plain string, no resolution needed
 * - DataReference: A UUID pointing to data that needs path resolution
 * - PathSubstitution: A template with placeholder that needs path substitution

 */
@Serializable
sealed interface ExpandedArg
{

    /**
     * A literal string argument with no special resolution needed.
     * @property value The literal string value
     */
    @Serializable
    data class Literal( val value: String ) : ExpandedArg

    /**
     * A reference to input or output data that must be resolved to a physical path.
     * Example:
     *   Direct reference: "input.my-data"
     *   Becomes: DataReference(id = UUID(...))
     *   Executor resolves to: "/artefacts/550e8400-.../data.csv"
     *
     * @property dataRefId The UUID of the data reference (from bindings)
     */
    @Serializable
    data class DataReference(
        val dataRefId: UUID,
    ) : ExpandedArg

    /**
     * A template string with a placeholder that must be substituted with a physical path.
     * Example:
     *   Path substitution: "--input=$(input.my-data)"
     *   Becomes: PathSubstitution(dataRefId = UUID(...), template = "--input=$()")
     *   Executor resolves to: "--input=/artefacts/550e8400-.../data.csv"
     *
     * @property dataRefId The UUID of the data reference (from bindings)
     * @property template The template string with $() placeholder for the path
     */
    @Serializable
    data class PathSubstitution(
        val dataRefId: UUID,
        val template: String
    ) : ExpandedArg
}
