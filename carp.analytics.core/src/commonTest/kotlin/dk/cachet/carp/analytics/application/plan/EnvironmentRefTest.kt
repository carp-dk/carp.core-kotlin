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
}
