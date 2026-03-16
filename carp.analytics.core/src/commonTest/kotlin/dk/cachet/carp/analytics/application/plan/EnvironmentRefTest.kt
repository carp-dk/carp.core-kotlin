package dk.cachet.carp.analytics.application.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnvironmentRefTest
{
    @Test
    fun conda_setup_command_includes_all_components()
    {
        val ref = CondaEnvironmentRef(
            id = "env-1",
            name = "myenv",
            dependencies = listOf("numpy", "scipy"),
            channels = listOf("conda-forge", "defaults"),
            pythonVersion = "3.11"
        )

        val cmd = ref.generateSetupCommand()

        assertTrue(cmd.contains("conda create"))
        assertTrue(cmd.contains("-n myenv"))
        assertTrue(cmd.contains("-c conda-forge"))
        assertTrue(cmd.contains("python=3.11"))
        assertTrue(cmd.contains("numpy scipy"))
    }

    @Test
    fun conda_execution_template_correct()
    {
        val ref = CondaEnvironmentRef("env-1", "myenv", emptyList())
        assertEquals("conda run -n myenv {executable} {args}", ref.generateExecutionTemplate())
    }

    @Test
    fun system_setup_empty()
    {
        val ref = SystemEnvironmentRef("env-sys")
        assertEquals("", ref.generateSetupCommand())
    }

    @Test
    fun all_templates_have_placeholders()
    {
        val refs: List<EnvironmentRef> = listOf(
            CondaEnvironmentRef("e1", "n", emptyList()),
            PixiEnvironmentRef("e2", emptyList()),
            SystemEnvironmentRef("e3")
        )

        refs.forEach { ref ->
            val template = ref.generateExecutionTemplate()
            assertTrue(template.contains("{executable}"), "${ref::class.simpleName} missing {executable}")
            assertTrue(template.contains("{args}"), "${ref::class.simpleName} missing {args}")
        }
    }
    @Test
    fun createsREnvironmentRef()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2", "dplyr")
        )

        assertEquals("r-env-001", ref.id)
        assertEquals("4.3.0", ref.rVersion)
        assertEquals(2, ref.rPackages.size)
    }

    @Test
    fun generatesDescriptiveName()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0"
        )

        assertEquals("R-4.3.0", ref.name)
    }

    @Test
    fun getExecutionTemplate()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0"
        )

        val template = ref.generateExecutionTemplate()

        assertTrue(template.contains("Rscript"))
        assertTrue(template.contains("{executable}"))
    }

    @Test
    fun acceptsRenvLockFile()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            renvLockFile = "/path/to/renv.lock"
        )

        assertEquals("/path/to/renv.lock", ref.renvLockFile)
    }

    @Test
    fun acceptsInstallationPath()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2"),
            installationPath = "/opt/R/4.3.0"
        )

        assertEquals("/opt/R/4.3.0", ref.installationPath)
    }

    @Test
    fun validatesSuccessfully()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2")
        )

        val errors = ref.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun validatesWithRenvLockFile()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            renvLockFile = "/path/to/renv.lock"
        )

        val errors = ref.validate()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun rejectsEmptyId()
    {
        val ref = REnvironmentRef(
            id = "",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2")
        )

        val errors = ref.validate()

        assertTrue(errors.any { it.contains("ID") })
    }

    @Test
    fun rejectsMissingPackagesAndLockFile()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = emptyList(),
            renvLockFile = null
        )

        val errors = ref.validate()

        assertTrue(errors.any { it.contains("renvLockFile or rPackages") })
    }

    @Test
    fun supportsEnvironmentVariables()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2"),
            environmentVariables = mapOf(
                "R_LIBS" to "/usr/local/lib/R/site-library",
                "R_HOME" to "/opt/R/4.3.0"
            )
        )

        assertEquals(2, ref.environmentVariables.size)
    }

    @Test
    fun supportsAdditionalDependencies()
    {
        val ref = REnvironmentRef(
            id = "r-env-001",
            rVersion = "4.3.0",
            rPackages = listOf("ggplot2"),
            dependencies = listOf("pandoc", "ghostscript")
        )

        assertEquals(2, ref.dependencies.size)
    }
}
