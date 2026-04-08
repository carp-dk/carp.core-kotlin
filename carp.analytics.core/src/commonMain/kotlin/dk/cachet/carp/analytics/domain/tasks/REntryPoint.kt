package dk.cachet.carp.analytics.domain.tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes *what* R code to execute — without specifying *how* to invoke the interpreter.
 *
 * An [REntryPoint] is purely declarative: it carries no runtime logic and makes no
 * assumptions about the R executable, environment activation, or working directory.
 * Resolution of those concerns is deferred to the plan/compile phase.
 *
 * Variant:
 * - [RScript] — run an `.R` or `.Rmd` file by path (relative paths are resolved at plan-time against the
 *   step's working directory or an explicit `RTaskDefinition.workingDirectory`).
 */
@Serializable
sealed interface REntryPoint

/**
 * Run an R script located at [scriptPath].
 *
 * [scriptPath] is a path to an `.R` or `.Rmd` file. It may be absolute or relative; relative paths
 * are resolved at plan-time and are therefore not validated here.
 */
@Serializable
@SerialName("RScript")
data class RScript( val scriptPath: String ) : REntryPoint
{
    init
    {
        require(scriptPath.isNotBlank()) { "RScript.scriptPath must not be blank" }
    }
}

