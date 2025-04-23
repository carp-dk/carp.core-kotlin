package dk.cachet.carp.analytics.infrastructure.environment

import dk.cachet.carp.analytics.domain.environment.Environment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CondaCommandGeneratorTest {

    private val generator = CondaCommandGenerator()

    @Test
    fun testGenerateSetupCommandWithValidEnvironment() {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy", "pandas"),
            channels = listOf("conda-forge"),
            pythonVersion = "3.9"
        )
        val expectedCommand = "conda create -n TestEnv numpy pandas -c conda-forge python=3.9"
        val result = generator.generateSetupCommand(condaEnv)
        assertEquals(expectedCommand, result)
    }

    @Test
    fun testGenerateActivateCommandWithValidEnvironment() {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy")
        )
        val expectedCommand = "conda activate TestEnv"
        val result = generator.generateActivateCommand(condaEnv)
        assertEquals(expectedCommand, result)
    }

    @Test
    fun testGenerateTeardownCommandWithValidEnvironment() {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = listOf("numpy")
        )
        val expectedCommand = "conda remove --name TestEnv --all"
        val result = generator.generateTeardownCommand(condaEnv)
        assertEquals(expectedCommand, result)
    }

    @Test
    fun testGenerateSetupCommandWithEmptyDependencies() {
        val condaEnv = CondaEnvironment(
            name = "TestEnv",
            dependencies = emptyList()
        )
        val expectedCommand = "conda create -n TestEnv -c defaults"
        val result = generator.generateSetupCommand(condaEnv)
        assertEquals(expectedCommand, result)
    }

    @Test
    fun testInvalidEnvironmentTypeThrowsException() {
        val fakeEnv = object : dk.cachet.carp.analytics.domain.environment.Environment
        {
            override val name = "FakeEnv"
            override val dependencies = listOf("numpy")
        }

        assertFailsWith<IllegalArgumentException> {
            generator.generateSetupCommand(fakeEnv)
        }
    }
    
}
