package dk.cachet.carp.analytics.application.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Interface for environment references.
 */
@Serializable
sealed interface EnvironmentRef
{
    val id: String
    val dependencies: List<String>

    /**
     * Returns setup command for preflight phase.
     * Empty string if no setup needed (e.g., system environment).
     */
    fun generateSetupCommand(): String

    /**
     * Returns execution template with {executable} and {args} placeholders.
     * Executor substitutes these at runtime.
     */
    fun generateExecutionTemplate(): String
}

@Serializable
@SerialName("conda")
data class CondaEnvironmentRef(
    override val id: String,
    val name: String,
    override val dependencies: List<String>,
    val channels: List<String> = listOf("conda-forge", "defaults"),
    val pythonVersion: String = "3.11"
) : EnvironmentRef
{

    override fun generateSetupCommand(): String
    {
        val channelFlags = channels.joinToString(" ") { "-c $it" }
        val depsStr = dependencies.joinToString(" ")
        return "conda create -n $name -y $channelFlags python=$pythonVersion $depsStr"
    }

    override fun generateExecutionTemplate(): String
    {
        return "conda run -n $name {executable} {args}"
    }
}

@Serializable
@SerialName("pixi")
data class PixiEnvironmentRef(
    override val id: String,
    override val dependencies: List<String>,
    val pythonVersion: String = "3.12"
) : EnvironmentRef
{

    override fun generateSetupCommand(): String
    {
        val depsStr = dependencies.joinToString(" ")
        return "pixi add $depsStr python=$pythonVersion"
    }

    override fun generateExecutionTemplate(): String
    {
        return "pixi run {executable} {args}"
    }
}

@Serializable
@SerialName("system")
data class SystemEnvironmentRef(
    override val id: String,
    override val dependencies: List<String> = emptyList()
) : EnvironmentRef
{

    override fun generateSetupCommand(): String = ""

    override fun generateExecutionTemplate(): String = "{executable} {args}"
}

/**
 * Runtime reference to an R environment.
 *
 * Serializable, immutable, created during planning phase.
 * Used to pass R environment specification to execution layer.
 *
 * Extends the sealed EnvironmentRef hierarchy:
 * - CondaEnvironmentRef
 * - PixiEnvironmentRef
 * - SystemEnvironmentRef
 * - REnvironmentRef ← NEW
 */
@Serializable
@SerialName("REnvironmentRef")
data class REnvironmentRef(
    override val id: String,
    val rVersion: String,
    val rPackages: List<String> = emptyList(),
    val renvLockFile: String? = null,
    val installationPath: String? = null,
    override val dependencies: List<String> = emptyList(),
    val environmentVariables: Map<String, String> = emptyMap()
) : EnvironmentRef
{

    /**
     * Descriptive name for this R environment.
     * Used in logs and displays.
     */
    val name: String
        get() = "R-$rVersion"

    /**
     * Get command template for executing in R environment.
     *
     * Example: "Rscript script.R"
     */
    override fun generateExecutionTemplate(): String
    {
        return "Rscript {executable} {args}"
    }

    /**
     * Validate this R environment reference.
     */
    fun validate(): List<String>
    {
        val errors = mutableListOf<String>()

        if (id.isBlank())
        {
            errors.add("Environment ID cannot be blank")
        }

        if (rVersion.isBlank())
        {
            errors.add("R version cannot be blank")
        }

        if (renvLockFile == null && rPackages.isEmpty())
        {
            errors.add("Either renvLockFile or rPackages must be specified")
        }

        return errors
    }

    override fun generateSetupCommand(): String
    {
        TODO("Not yet implemented")
    }
}
