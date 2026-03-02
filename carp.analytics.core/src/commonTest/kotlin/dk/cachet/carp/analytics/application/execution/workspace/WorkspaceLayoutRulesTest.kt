package dk.cachet.carp.analytics.application.execution.workspace

import kotlin.test.*

/**
 * Tests for [WorkspaceLayoutRules] to ensure constants are properly defined.
 */
class WorkspaceLayoutRulesTest
{
    @Test
    fun constants_have_expected_values()
    {
        assertEquals("steps", WorkspaceLayoutRules.STEPS_DIR)
        assertEquals("inputs", WorkspaceLayoutRules.INPUTS_DIR)
        assertEquals("outputs", WorkspaceLayoutRules.OUTPUTS_DIR)
        assertEquals("logs", WorkspaceLayoutRules.LOGS_DIR)
    }

    @Test
    fun constants_are_not_empty()
    {
        assertTrue(WorkspaceLayoutRules.STEPS_DIR.isNotEmpty(), "STEPS_DIR should not be empty")
        assertTrue(WorkspaceLayoutRules.INPUTS_DIR.isNotEmpty(), "INPUTS_DIR should not be empty")
        assertTrue(WorkspaceLayoutRules.OUTPUTS_DIR.isNotEmpty(), "OUTPUTS_DIR should not be empty")
        assertTrue(WorkspaceLayoutRules.LOGS_DIR.isNotEmpty(), "LOGS_DIR should not be empty")
    }

    @Test
    fun constants_do_not_contain_path_separators()
    {
        val constants = listOf(
            WorkspaceLayoutRules.STEPS_DIR,
            WorkspaceLayoutRules.INPUTS_DIR,
            WorkspaceLayoutRules.OUTPUTS_DIR,
            WorkspaceLayoutRules.LOGS_DIR
        )

        constants.forEach { constant ->
            assertFalse(constant.contains("/"), "$constant should not contain forward slash")
            assertFalse(constant.contains("\\"), "$constant should not contain backslash")
            assertFalse(constant.contains(".."), "$constant should not contain path traversal")
        }
    }
}
