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
 * - DataReference: A UUID pointing to input/output data
 * - PathSubstitution: A template with placeholder that needs path substitution at execution time
 * - EnvironmentVariable: An environment variable reference
 */
@Serializable
sealed interface ExpandedArg
{
    /**
     * A literal string argument with no special resolution needed.
     *
     * @property value The literal string value
     */
    @Serializable
    data class Literal( val value: String ) : ExpandedArg

    /**
     * A reference to input or output data (by UUID).
     *
     * Example:
     *   Direct reference: "input.my-data" → DataReference(id = UUID(...))
     *   Executor resolves via bindings to get ResolvedInput/ResolvedOutput
     *   Then uses physical path from resolved structure
     *
     * @property id The UUID of the input or output data spec
     */
    @Serializable
    data class DataReference( val id: UUID ) : ExpandedArg

    /**
     * A template string with a placeholder that must be substituted with a physical path.
     *
     * Used for arguments like `--input=$(input.my-data)` where:
     * - At plan time: becomes `--input=()` with the data ID recorded
     * - At execution time: the `()` is replaced with the actual path
     *
     * Example:
     *   Path template: "--input=$(input.my-data)"
     *   Becomes: PathSubstitution(id = UUID(...), template = "--input=$()")
     *   Executor resolves to: "--input=/workspace/outputs/step-id/data.csv"
     *
     * @property id The UUID of the input or output data spec
     * @property template The template string with `()` placeholder for path substitution
     */
    @Serializable
    data class PathSubstitution(
        val id: UUID,
        val template: String
    ) : ExpandedArg

    /**
     * An environment variable reference.
     *
     * Used for arguments like `--model=$(env.MODEL_PATH)` where:
     * - At plan time: recognized but NOT resolved (environment may not exist yet)
     * - At execution time: the `$(env.VAR_NAME)` is replaced with the actual environment variable
     *
     * Example:
     *   Env reference: "--model=$(env.MODEL_PATH)"
     *   Stays as: EnvironmentVariable(name = "MODEL_PATH", template = "--model=$()")
     *   Executor resolves to: "--model=/models/v2.pkl"
     *
     * @property name The environment variable name (without $( env. prefix))
     * @property template The template string with `()` placeholder for substitution
     */
    @Serializable
    data class EnvironmentVariable(
        val name: String,
        val template: String
    ) : ExpandedArg
}
