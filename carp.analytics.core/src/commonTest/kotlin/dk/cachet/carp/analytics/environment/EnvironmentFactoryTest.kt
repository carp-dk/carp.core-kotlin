package dk.cachet.carp.analytics.environment

import dk.cachet.carp.analytics.application.environment.EnvironmentFactory
import dk.cachet.carp.analytics.infrastructure.environment.CondaEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvironmentFactoryTest
{

    @Test
    fun testCreateCondaEnvironment()
    {
        val config = mapOf(
            "name" to "TestEnv",
            "dependencies" to listOf("numpy", "pandas"),
            "channels" to listOf("conda-forge"),
            "pythonVersion" to "3.9"
        )

        val condaEnv = EnvironmentFactory.create("conda", config)

        assertEquals("TestEnv", condaEnv.name)
        assertEquals(listOf("numpy", "pandas"), (condaEnv as CondaEnvironment).dependencies)
        assertEquals(listOf("conda-forge"), condaEnv.channels)
        assertEquals("3.9", condaEnv.pythonVersion)
    }

    @Test
    fun testUnknownEnvironmentTypeThrowsException()
    {
        assertFailsWith<IllegalArgumentException> {
            EnvironmentFactory.create("unknown", emptyMap())
        }
    }
}
