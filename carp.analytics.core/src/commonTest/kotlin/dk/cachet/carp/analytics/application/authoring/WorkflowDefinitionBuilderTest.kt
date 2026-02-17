package dk.cachet.carp.analytics.application.authoring

import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.analytics.domain.tasks.TaskDefinition
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Comprehensive test suite for [WorkflowDefinitionBuilder].
 *
 * Tests all builder methods and functionality to maximize code coverage.
 */
class WorkflowDefinitionBuilderTest
{
    private lateinit var metadata: WorkflowMetadata

    @BeforeTest
    fun setup()
    {
        metadata = WorkflowMetadata(
            name = "Test Workflow",
            description = "A test workflow for builder tests",
            id = UUID.randomUUID(),
            version = Version(1, 0)
        )
    }

    @Test
    fun `builder can be instantiated with workflow metadata`()
    {
        // Arrange & Act
        val builder = WorkflowDefinitionBuilder(metadata)

        // Assert
        assertNotNull(builder)
    }

    @Test
    fun `addComponent adds a single component to the builder`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val component = createMockStep("Step 1")

        // Act
        builder.addComponent(component)
        val definition = builder.build()

        // Assert
        assertEquals(1, definition.workflow.getComponents().size)
        assertEquals(component, definition.workflow.getComponents()[0])
    }

    @Test
    fun `addComponent returns the builder for method chaining`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val component = createMockStep("Step 1")

        // Act
        val result = builder.addComponent(component)

        // Assert
        assertSame(builder, result)
    }

    @Test
    fun `addComponent can be called multiple times to add multiple components`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val step1 = createMockStep("Step 1")
        val step2 = createMockStep("Step 2")
        val step3 = createMockStep("Step 3")

        // Act
        builder.addComponent(step1)
            .addComponent(step2)
            .addComponent(step3)
        val definition = builder.build()

        // Assert
        assertEquals(3, definition.workflow.getComponents().size)
        assertEquals(step1, definition.workflow.getComponents()[0])
        assertEquals(step2, definition.workflow.getComponents()[1])
        assertEquals(step3, definition.workflow.getComponents()[2])
    }

    @Test
    fun `addEnvironment adds a single environment to the builder`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val environment = createMockEnvironment("test-env")

        // Act
        builder.addEnvironment(environment)
        val definition = builder.build()

        // Assert
        assertEquals(1, definition.environments.size)
        assertTrue(definition.environments.containsKey(environment.id))
        assertEquals(environment, definition.environments[environment.id])
    }

    @Test
    fun `addEnvironment returns the builder for method chaining`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val environment = createMockEnvironment("test-env")

        // Act
        val result = builder.addEnvironment(environment)

        // Assert
        assertSame(builder, result)
    }

    @Test
    fun `addEnvironment can be called multiple times to add multiple environments`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")
        val env3 = createMockEnvironment("env-3")

        // Act
        builder.addEnvironment(env1)
            .addEnvironment(env2)
            .addEnvironment(env3)
        val definition = builder.build()

        // Assert
        assertEquals(3, definition.environments.size)
        assertTrue(definition.environments.containsKey(env1.id))
        assertTrue(definition.environments.containsKey(env2.id))
        assertTrue(definition.environments.containsKey(env3.id))
    }

    @Test
    fun `addEnvironments adds multiple environments at once via varargs`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")
        val env3 = createMockEnvironment("env-3")

        // Act
        builder.addEnvironments(env1, env2, env3)
        val definition = builder.build()

        // Assert
        assertEquals(3, definition.environments.size)
        assertTrue(definition.environments.containsKey(env1.id))
        assertTrue(definition.environments.containsKey(env2.id))
        assertTrue(definition.environments.containsKey(env3.id))
    }

    @Test
    fun `addEnvironments returns the builder for method chaining`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")

        // Act
        val result = builder.addEnvironments(env1, env2)

        // Assert
        assertSame(builder, result)
    }

    @Test
    fun `addEnvironments with single environment works correctly`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env = createMockEnvironment("single-env")

        // Act
        builder.addEnvironments(env)
        val definition = builder.build()

        // Assert
        assertEquals(1, definition.environments.size)
        assertTrue(definition.environments.containsKey(env.id))
    }

    @Test
    fun `addEnvironments with no arguments does nothing`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)

        // Act
        builder.addEnvironments()
        val definition = builder.build()

        // Assert
        assertEquals(0, definition.environments.size)
    }

    @Test
    fun `build returns a WorkflowDefinition with correct metadata`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)

        // Act
        val definition = builder.build()

        // Assert
        assertNotNull(definition)
        assertEquals(metadata, definition.workflow.metadata)
    }

    @Test
    fun `build creates a Workflow with added components`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val step = createMockStep("Test Step")
        builder.addComponent(step)

        // Act
        val definition = builder.build()

        // Assert
        assertEquals(1, definition.workflow.getComponents().size)
        assertEquals(step, definition.workflow.getComponents()[0])
    }

    @Test
    fun `build includes all added environments`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")
        builder.addEnvironment(env1)
            .addEnvironment(env2)

        // Act
        val definition = builder.build()

        // Assert
        assertEquals(2, definition.environments.size)
        assertEquals(env1, definition.environments[env1.id])
        assertEquals(env2, definition.environments[env2.id])
    }

    @Test
    fun `build creates immutable definition with empty components`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)

        // Act
        val definition = builder.build()

        // Assert
        assertNotNull(definition)
        assertEquals(0, definition.workflow.getComponents().size)
    }

    @Test
    fun `build creates immutable definition with empty environments`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)

        // Act
        val definition = builder.build()

        // Assert
        assertNotNull(definition)
        assertEquals(0, definition.environments.size)
    }

    @Test
    fun `hasEnvironment returns true for added environment`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val environment = createMockEnvironment("test-env")
        builder.addEnvironment(environment)

        // Act
        val result = builder.hasEnvironment(environment.id)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `hasEnvironment returns false for non-existent environment`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val nonExistentId = UUID.randomUUID()

        // Act
        val result = builder.hasEnvironment(nonExistentId)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `hasEnvironment returns false when no environments added`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val someId = UUID.randomUUID()

        // Act
        val result = builder.hasEnvironment(someId)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `hasEnvironment returns true after multiple environments added`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")
        val env3 = createMockEnvironment("env-3")
        builder.addEnvironments(env1, env2, env3)

        // Act
        val result = builder.hasEnvironment(env2.id)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `builder can mix component and environment additions`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val step1 = createMockStep("Step 1")
        val env1 = createMockEnvironment("env-1")
        val step2 = createMockStep("Step 2")
        val env2 = createMockEnvironment("env-2")

        // Act
        builder.addComponent(step1)
            .addEnvironment(env1)
            .addComponent(step2)
            .addEnvironment(env2)
        val definition = builder.build()

        // Assert
        assertEquals(2, definition.workflow.getComponents().size)
        assertEquals(2, definition.environments.size)
        assertEquals(step1, definition.workflow.getComponents()[0])
        assertEquals(step2, definition.workflow.getComponents()[1])
        assertTrue(definition.environments.containsKey(env1.id))
        assertTrue(definition.environments.containsKey(env2.id))
    }

    @Test
    fun `multiple builds from same builder produce consistent results`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val step = createMockStep("Test Step")
        val env = createMockEnvironment("test-env")
        builder.addComponent(step)
            .addEnvironment(env)

        // Act
        val definition1 = builder.build()
        val definition2 = builder.build()

        // Assert
        assertEquals(definition1.workflow.getComponents().size, definition2.workflow.getComponents().size)
        assertEquals(definition1.environments.size, definition2.environments.size)
    }

    @Test
    fun `builder preserves workflow metadata through build`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)

        // Act
        val definition = builder.build()

        // Assert
        assertEquals(metadata.name, definition.workflow.metadata.name)
        assertEquals(metadata.description, definition.workflow.metadata.description)
        assertEquals(metadata.id, definition.workflow.metadata.id)
        assertEquals(metadata.version, definition.workflow.metadata.version)
    }

    @Test
    fun `environments are stored as associative map by id`()
    {
        // Arrange
        val builder = WorkflowDefinitionBuilder(metadata)
        val env1 = createMockEnvironment("env-1")
        val env2 = createMockEnvironment("env-2")
        builder.addEnvironments(env1, env2)

        // Act
        val definition = builder.build()

        // Assert
        val environmentMap = definition.environments
        assertEquals(env1, environmentMap[env1.id])
        assertEquals(env2, environmentMap[env2.id])
    }

    // Helper methods to create mock objects

    private fun createMockStep( name: String ): Step
    {
        // Create a mock process (this is a simplified version)
        // Since we're testing the builder, not the process execution,
        // we just need something that implements WorkflowComponent
        return Step(
            metadata = StepMetadata(
                name = name,
                id = UUID.randomUUID(),
                description = "Mock step for testing"
            ),
            task = MockTask(),
            environmentId = UUID.randomUUID()
        )
    }

    private fun createMockEnvironment( name: String ): EnvironmentDefinition
    {
        return MockEnvironmentDefinition(
            id = UUID.randomUUID(),
            name = name,
            dependencies = listOf("dependency1", "dependency2"),
            environmentVariables = mapOf("VAR1" to "value1", "VAR2" to "value2")
        )
    }

    private data class MockEnvironmentDefinition(
        override val id: UUID,
        override val name: String,
        override val dependencies: List<String> = emptyList(),
        override val environmentVariables: Map<String, String> = emptyMap()
    ) : EnvironmentDefinition

    // Mock task for testing
    private class MockTask : TaskDefinition
    {
        override val id: UUID = UUID.randomUUID()
        override val name: String = "MockTask"
        override val description: String = "A mock task for testing"
    }
}




