package dk.cachet.carp.analytics.application.environment

import dk.cachet.carp.analytics.domain.environment.CommandGenerator
import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DefaultCommandGeneratorRegistryTest
{
    // Test environment implementation
    private data class TestEnvironmentDefinition(
        override val id: UUID,
        override val name: String,
        override val dependencies: List<String> = emptyList(),
        override val environmentVariables: Map<String, String> = emptyMap()
    ) : EnvironmentDefinition

    // Test command generator implementation
    private class TestCommandGenerator : CommandGenerator
    {
        override fun generateSetupCommand( env: EnvironmentDefinition ): String = "setup ${env.name}"
        override fun generateActivateCommand( env: EnvironmentDefinition ): String = "activate ${env.name}"
        override fun generateTeardownCommand( env: EnvironmentDefinition ): String = "teardown ${env.name}"
        override fun generateRunCommand(
            env: EnvironmentDefinition,
            command: String
        ): String = "run $command on ${env.name}"
        override fun generateListEnvironmentsCommand(): String = "list"
        override fun generateCreateEnvironmentCommand( env: EnvironmentDefinition ): String = "create ${env.name}"
        override fun generateInstallDependenciesCommand(
            env: EnvironmentDefinition
        ): String? = if (env.name.isNotEmpty()) "install on ${env.name}" else null
        override fun parseEnvironmentList(
            output: String
        ): List<String> = output.split("\n").filter { it.isNotBlank() }
    }

    @Test
    fun `registry properly implements CommandGeneratorResolver interface`()
    {
        // Clear any existing registrations
        DefaultCommandGeneratorRegistry.clear()

        val testEnv = TestEnvironmentDefinition(UUID.randomUUID(), "test-env", emptyList())
        val testGenerator = TestCommandGenerator()

        // Test registration by string name (simplified API)
        DefaultCommandGeneratorRegistry.register("TestEnvironmentDefinition", testGenerator)

        // Test keySelector function
        val key = DefaultCommandGeneratorRegistry.keySelector(testEnv)
        assertEquals("TestEnvironmentDefinition", key)

        // Test registry contains the generator
        assertTrue(DefaultCommandGeneratorRegistry.registry.containsKey("TestEnvironmentDefinition"))
        assertEquals(testGenerator, DefaultCommandGeneratorRegistry.registry["TestEnvironmentDefinition"])

        // Test get method
        val retrievedGenerator = DefaultCommandGeneratorRegistry.get(testEnv)
        assertEquals(testGenerator, retrievedGenerator)
    }

    @Test
    fun `registration by string name works correctly`()
    {
        DefaultCommandGeneratorRegistry.clear()

        val testGenerator = TestCommandGenerator()
        DefaultCommandGeneratorRegistry.register("CustomEnvironment", testGenerator)

        assertTrue(DefaultCommandGeneratorRegistry.registry.containsKey("CustomEnvironment"))
        assertEquals(testGenerator, DefaultCommandGeneratorRegistry.registry["CustomEnvironment"])
    }

    @Test
    fun `get throws error for unregistered environment`()
    {
        DefaultCommandGeneratorRegistry.clear()

        val testEnv = TestEnvironmentDefinition(UUID.randomUUID(), "unknown-env", emptyList())

        assertFailsWith<IllegalStateException> {
            DefaultCommandGeneratorRegistry.get(testEnv)
        }
    }

    @Test
    fun `clear removes all registrations`()
    {
        val testGenerator = TestCommandGenerator()
        DefaultCommandGeneratorRegistry.register("TestEnv", testGenerator)

        assertTrue(DefaultCommandGeneratorRegistry.registry.isNotEmpty())

        DefaultCommandGeneratorRegistry.clear()

        assertTrue(DefaultCommandGeneratorRegistry.registry.isEmpty())
    }
}
