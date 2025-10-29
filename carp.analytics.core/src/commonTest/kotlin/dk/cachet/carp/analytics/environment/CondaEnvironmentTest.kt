package dk.cachet.carp.analytics.environment


import dk.cachet.carp.analytics.infrastructure.environment.CondaEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CondaEnvironmentTest
{

    @Test
    fun testValidCondaEnvironmentCreation()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy", "pandas"),
            channels = listOf("conda-forge"),
            pythonVersion = "3.9"
        )

        assertEquals("TestEnv", condaEnv.name)
        assertEquals(listOf("numpy", "pandas"), condaEnv.dependencies)
        assertEquals(listOf("conda-forge"), condaEnv.channels)
        assertEquals("3.9", condaEnv.pythonVersion)
    }

    @Test
    fun testCondaEnvironmentCreationWithNoDependencies()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = emptyList()
        )

        assertEquals("TestEnv", condaEnv.name)
        assertEquals(emptyList<String>(), condaEnv.dependencies)
        assertEquals(listOf("defaults"), condaEnv.channels) // Default value
        assertEquals(null, condaEnv.pythonVersion)
    }

    @Test
    fun testDefaultChannelsAreApplied()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy")
        )

        assertEquals(listOf("defaults"), condaEnv.channels)
    }

    @Test
    fun testDependenciesWithSpecificVersions()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy", "matplotlib=3.1.0")
        )

        assertEquals(listOf("numpy", "matplotlib=3.1.0"), condaEnv.dependencies)
    }

    @Test
    fun testEnvironmentNameIsNotNull()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy")
        )

        assertNotNull(condaEnv.name)
        assertEquals("TestEnv", condaEnv.name)
    }

    @Test
    fun testEnvironmentWithoutPythonVersion()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy")
        )

        assertEquals(null, condaEnv.pythonVersion)
    }
}
