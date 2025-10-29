package dk.cachet.carp.analytics.environment

import dk.cachet.carp.analytics.application.environment.CommandGeneratorFactory
import dk.cachet.carp.analytics.domain.environment.Environment
import dk.cachet.carp.analytics.infrastructure.environment.CondaCommandGenerator
import dk.cachet.carp.analytics.infrastructure.environment.CondaEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandGeneratorFactoryTest
{

    @Test
    fun testGetGeneratorForRegisteredCondaEnvironment()
    {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy", "pandas"),
            channels = listOf("conda-forge"),
            pythonVersion = "3.9"
        )

        val generator = CommandGeneratorFactory.getGenerator(condaEnv)
        assertEquals(CondaCommandGenerator::class, generator::class)
    }

    @Test
    fun testGetGeneratorForUnregisteredEnvironmentThrowsException()
    {
        val fakeEnv =
            object : Environment
            {
                override val name = "FakeEnv"
                override val dependencies = listOf("numpy")
            }

        assertFailsWith<IllegalArgumentException>{
            CommandGeneratorFactory.getGenerator(fakeEnv)
        }
    }
}
