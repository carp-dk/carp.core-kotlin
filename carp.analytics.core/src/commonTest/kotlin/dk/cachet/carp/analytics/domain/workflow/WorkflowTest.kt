package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.analytics.domain.data.FileLocation
import dk.cachet.carp.analytics.domain.data.InMemoryLocation
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import dk.cachet.carp.analytics.domain.environment.SystemEnvironmentDefinition
import dk.cachet.carp.analytics.domain.tasks.CommandTaskDefinition
import dk.cachet.carp.analytics.domain.tasks.Literal
import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Tests for [Workflow], [WorkflowDefinition], [Step] validation and [StepsLock].
 */
class WorkflowTest
{
    private val json = createDefaultJSON( CoreAnalyticsSerializer.module )
    private val environmentId = UUID.randomUUID()

    private fun step(
        name: String,
        inputs: List<InputDataSpec> = emptyList(),
        outputs: List<OutputDataSpec> = emptyList()
    ) = Step(
        metadata = StepMetadata( name, UUID.randomUUID(), descriptorId = "core.$name" ),
        inputs = inputs,
        outputs = outputs,
        task = CommandTaskDefinition( UUID.randomUUID(), name, executable = "echo", args = listOf( Literal( "hi" ) ) ),
        environmentId = environmentId
    )

    private fun workflow() = Workflow( WorkflowMetadata( "Sleep", "Nightly", UUID.randomUUID(), Version( 2, 1 ) ) )

    @Test
    fun components_can_be_added_and_removed()
    {
        val workflow = workflow()
        val first = step( "a" )
        val second = step( "b" )
        val third = step( "c" )

        workflow.addComponent( first )
        workflow.addComponents( listOf( second, third ) )
        workflow.removeComponent( second )

        assertEquals( listOf<WorkflowComponent>( first, third ), workflow.getComponents() )
    }

    @Test
    fun getComponents_returns_a_copy()
    {
        val workflow = workflow()
        val components = workflow.getComponents()

        workflow.addComponent( step( "a" ) )

        assertTrue( components.isEmpty() )
    }

    @Test
    fun can_serialize_and_deserialize_WorkflowDefinition_with_steps()
    {
        val workflow = workflow()
        workflow.addComponents( listOf( step( "a" ), step( "b" ) ) )
        val definition = WorkflowDefinition(
            workflow,
            mapOf( environmentId to SystemEnvironmentDefinition( environmentId, "system" ) )
        )

        val serialized = json.encodeToString( WorkflowDefinition.serializer(), definition )
        val parsed = json.decodeFromString( WorkflowDefinition.serializer(), serialized )

        assertEquals( definition, parsed )
        assertEquals( workflow.getComponents(), parsed.workflow.getComponents() )
    }

    @Test
    fun version_toString_omits_missing_minor()
    {
        assertEquals( "2", Version( 2 ).toString() )
        assertEquals( "2.1", Version( 2, 1 ).toString() )
    }

    @Test
    fun can_serialize_and_deserialize_SubWorkflowMetadata()
    {
        val metadata = SubWorkflowMetadata( "sub", UUID.randomUUID(), "part" )

        val serialized = json.encodeToString( SubWorkflowMetadata.serializer(), metadata )

        assertEquals( metadata, json.decodeFromString( SubWorkflowMetadata.serializer(), serialized ) )
        assertEquals( Version( 1 ), metadata.version )
    }

    @Test
    fun step_with_valid_data_specs_is_valid()
    {
        val step = step(
            "a",
            inputs = listOf( InputDataSpec( UUID.randomUUID(), "in", location = FileLocation( "in.csv" ) ) ),
            outputs = listOf( OutputDataSpec( UUID.randomUUID(), "out", location = InMemoryLocation( "k" ) ) )
        )

        assertTrue( step.validate().isValid )
    }

    @Test
    fun step_collects_errors_from_inputs_and_outputs()
    {
        val step = step(
            "a",
            inputs = listOf( InputDataSpec( UUID.randomUUID(), "in", location = FileLocation() ) ),
            outputs = listOf( OutputDataSpec( UUID.randomUUID(), "out", location = InMemoryLocation( "" ) ) )
        )

        val result = step.validate()

        assertFalse( result.isValid )
        assertIs<StepValidationResult.Invalid>( result )
        assertEquals( 2, result.errors.size )
    }

    @Test
    fun steps_lock_finds_entry_by_uses()
    {
        val entry = LockedStep( "core.a", "1.0", "hash" )
        val lock = StepsLock( listOf( entry ) )

        assertEquals( entry, lock.entryFor( "core.a" ) )
        assertNull( lock.entryFor( "core.b" ) )
        assertEquals( lock, json.decodeFromString( StepsLock.serializer(), json.encodeToString( StepsLock.serializer(), lock ) ) )
    }
}
