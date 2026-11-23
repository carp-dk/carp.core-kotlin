package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.domain.environment.EnvironmentDefinition
import dk.cachet.carp.analytics.domain.tasks.CommandTaskDefinition
import dk.cachet.carp.analytics.domain.workflow.Step
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.domain.workflow.WorkflowDefinition
import dk.cachet.carp.analytics.domain.workflow.WorkflowMetadata
import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for [WorkflowServiceRequest].
 */
class WorkflowServiceRequestTest
{
    @Serializable
    @SerialName( "TestEnvironment" )
    data class TestEnvironment(
        override val id: UUID,
        override val name: String,
        override val dependencies: List<String> = emptyList(),
        override val environmentVariables: Map<String, String> = emptyMap()
    ) : EnvironmentDefinition

    private val json = createDefaultJSON(
        SerializersModule {
            include( CoreAnalyticsSerializer.module )
            polymorphic( EnvironmentDefinition::class ) { subclass( TestEnvironment::class ) }
        }
    )

    @Test
    fun create_workflow_request_keeps_steps_and_environments()
    {
        val environment = TestEnvironment( UUID.randomUUID(), "env" )
        val workflow = Workflow( WorkflowMetadata( name = "Gait", id = UUID.randomUUID() ) )
        workflow.addComponent(
            Step(
                metadata = StepMetadata( name = "Clean", id = UUID.randomUUID() ),
                task = CommandTaskDefinition( UUID.randomUUID(), "clean", executable = "python" ),
                environmentId = environment.id
            )
        )
        val request = WorkflowServiceRequest.CreateWorkflow(
            UUID.randomUUID(),
            WorkflowDefinition( workflow, mapOf( environment.id to environment ) )
        )

        val serialized = json.encodeToString( WorkflowServiceRequest.Serializer, request )
        val parsed = json.decodeFromString( WorkflowServiceRequest.Serializer, serialized )
            as WorkflowServiceRequest.CreateWorkflow

        assertEquals( request.definition.environments, parsed.definition.environments )
        assertEquals( workflow.getComponents(), parsed.definition.workflow.getComponents() )
    }
}
