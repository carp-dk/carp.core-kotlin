package dk.cachet.carp.analytics.domain.tasks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Describes *what* Python code to execute — without specifying *how* to invoke the interpreter.
 *
 * A [PythonEntryPoint] is purely declarative: it carries no runtime logic and makes no
 * assumptions about the Python executable, environment activation, or working directory.
 * Resolution of those concerns is deferred to the plan/compile phase.
 *
 * Two variants are supported:
 * - [Script] — run a `.py` file by path (relative paths are resolved at plan-time against the
 *   step's working directory or an explicit `PythonTaskDefinition.workingDirectory`).
 * - [Module] — run a top-level Python module via `python -m <module>`.
 */
@Serializable
sealed interface PythonEntryPoint

/**
 * Run a Python script located at [scriptPath].
 *
 * [scriptPath] is a path to a `.py` file. It may be absolute or relative; relative paths
 * are resolved at plan-time and are therefore not validated here.
 */
@Serializable
@SerialName("Script")
data class Script( val scriptPath: String ) : PythonEntryPoint
{
    init
    {
        require(scriptPath.isNotBlank()) { "Script.scriptPath must not be blank" }
    }
}

/**
 * Run a Python module by fully-qualified [moduleName] (equivalent to `python -m <moduleName>`).
 *
 * The module name must be a non-blank dotted identifier (e.g., `"mypackage.cli"`).
 * No validation of the dotted-identifier format is enforced here; that belongs to the planner.
 */
@Serializable
@SerialName("Module")
data class Module( val moduleName: String ) : PythonEntryPoint
{
    init
    {
        require(moduleName.isNotBlank()) { "Module.moduleName must not be blank" }
    }
}

