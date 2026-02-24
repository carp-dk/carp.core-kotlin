package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single token in a command argument list.
 *
 * This is a strongly-typed alternative to shell string interpolation.
 */
@Serializable
sealed interface ArgToken

/**
 * A raw command argument passed as-is.
 *
 * @param value The literal string value to include in the command
 */
@Serializable
@SerialName("Literal")
data class Literal( val value: String ) : ArgToken

/**
 * Refers to a declared InputDataSpec in the Step.
 * At plan-time this resolves to the concrete input path.
 *
 * @param inputId The UUID identifier of the input data to reference
 */
@Serializable
@SerialName("InputRef")
data class InputRef( val inputId: UUID ) : ArgToken

/**
 * Refers to a declared OutputDataSpec in the Step.
 * At plan-time this resolves to the concrete output path.
 *
 * @param outputId The UUID identifier of the output data to reference
 */
@Serializable
@SerialName("OutputRef")
data class OutputRef( val outputId: UUID ) : ArgToken

/**
 * Refers to a named, non-data parameter defined on the Step.
 *
 * Note: This is an optional/future feature.
 *
 * @param name The name of the parameter to reference
 */
@Serializable
@SerialName("ParamRef")
data class ParamRef( val name: String ) : ArgToken
