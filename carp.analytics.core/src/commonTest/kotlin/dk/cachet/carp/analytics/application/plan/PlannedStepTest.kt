package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.FileSystemSource
import dk.cachet.carp.analytics.domain.data.FileDestination
import dk.cachet.carp.analytics.domain.data.InputDataSpec
import dk.cachet.carp.analytics.domain.data.OutputDataSpec
import dk.cachet.carp.analytics.domain.data.FileFormat
import dk.cachet.carp.analytics.domain.data.WriteMode
import dk.cachet.carp.analytics.domain.workflow.StepMetadata
import dk.cachet.carp.analytics.domain.workflow.Version
import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertIs

/**
 * Tests for [PlannedStep] serialization and binding resolution.
 *
 * Verifies that planned steps correctly preserve:
 * - Step metadata (id, name, version, description)
 * - Process specifications (CommandSpec, InTasksRun)
 * - ResolvedBindings with ResolvedInput and ResolvedOutput
 * - Environment references
 */
class PlannedStepTest
{
    // Helper to create ResolvedInput
    private fun createResolvedInput(
        id: UUID = UUID.randomUUID(),
        name: String = "test-input"
    ): ResolvedInput
    {
        val spec = InputDataSpec(
            id = id,
            name = name,
            source = FileSystemSource(
                path = "input.csv",
                format = FileFormat.CSV
            )
        )
        val source = ResolvedDataSource.FileSystem(
            original = spec.source as FileSystemSource,
            resolvedPath = "/workspace/input/input.csv"
        )
        return ResolvedInput( spec = spec, resolvedSource = source )
    }

    // Helper to create ResolvedOutput
    private fun createResolvedOutput(
        id: UUID = UUID.randomUUID(),
        name: String = "test-output"
    ): ResolvedOutput
    {
        val spec = OutputDataSpec(
            id = id,
            name = name,
            destination = FileDestination(
                path = "output.csv",
                format = FileFormat.CSV,
                overwrite = false,
                writeMode = WriteMode.ERROR_IF_EXISTS
            )
        )
        val destination = ResolvedDataDestination.File(
            original = spec.destination as FileDestination,
            resolvedPath = "/workspace/output/output.csv"
        )
        return ResolvedOutput( spec = spec, resolvedDestination = destination )
    }

    @Test
    fun `serialization round-trip preserves CommandSpec planned step`()
    {
        val stepId = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val environmentId = UUID.randomUUID()

        val inputBinding = createResolvedInput( id = inputId, name = "input-data" )
        val outputBinding = createResolvedOutput( id = outputId, name = "output-data" )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Example Command Step",
                description = "A test command step",
                version = Version( 1, 0 )
            ),
            process = CommandSpec(
                executable = "echo",
                args = listOf( ExpandedArg.Literal( "hello" ) )
            ),
            bindings = ResolvedBindings(
                inputs = mapOf( inputId to inputBinding ),
                outputs = mapOf( outputId to outputBinding )
            ),
            environmentRef = environmentId
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( step )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PlannedStep>( encoded )

        assertEquals( step, decoded )
        assertEquals( stepId, decoded.metadata.id )
        assertEquals( "Example Command Step", decoded.metadata.name )
        assertEquals( environmentId, decoded.environmentRef )
    }

    @Test
    fun `serialization round-trip preserves InTasksRun planned step`()
    {
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val environmentId = UUID.randomUUID()

        val outputBinding = createResolvedOutput( id = outputId, name = "results" )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Example In-Process Step",
                version = Version( 1 )
            ),
            process = InTasksRun(
                operationId = "analysis.example.v1",
                parameters = mapOf( "key" to "value" )
            ),
            bindings = ResolvedBindings(
                inputs = emptyMap(),
                outputs = mapOf( outputId to outputBinding )
            ),
            environmentRef = environmentId
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( step )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PlannedStep>( encoded )

        assertEquals( step, decoded )
        assertEquals( stepId, decoded.metadata.id )
        assertEquals( "Example In-Process Step", decoded.metadata.name )
    }

    @Test
    fun `metadata preserves name and version`()
    {
        val stepId = UUID.randomUUID()

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Test Step",
                description = "Test description",
                version = Version( 2, 3 )
            ),
            process = InTasksRun( "op" ),
            bindings = ResolvedBindings(),
            environmentRef = UUID.randomUUID()
        )

        assertEquals( "Test Step", step.metadata.name )
        assertEquals( "Test description", step.metadata.description )
        assertEquals( 2, step.metadata.version.major )
        assertEquals( 3, step.metadata.version.minor )
    }

    @Test
    fun `bindings are preserved and accessible via inputs`()
    {
        val stepId = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val inputBinding = createResolvedInput( id = inputId, name = "my-input" )
        val outputBinding = createResolvedOutput( id = outputId, name = "my-output" )

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to inputBinding ),
            outputs = mapOf( outputId to outputBinding )
        )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Step With Bindings",
                version = Version( 1 )
            ),
            process = InTasksRun( "operation" ),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        // Verify input binding is accessible
        val retrievedInput = step.bindings.input( inputId )
        assertNotNull( retrievedInput )
        assertEquals( "my-input", retrievedInput.spec.name )
        assertEquals( inputId, retrievedInput.spec.id )
    }

    @Test
    fun `bindings are preserved and accessible via outputs`()
    {
        val stepId = UUID.randomUUID()
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val inputBinding = createResolvedInput( id = inputId, name = "my-input" )
        val outputBinding = createResolvedOutput( id = outputId, name = "my-output" )

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to inputBinding ),
            outputs = mapOf( outputId to outputBinding )
        )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Step With Bindings",
                version = Version( 1 )
            ),
            process = InTasksRun( "operation" ),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        // Verify output binding is accessible
        val retrievedOutput = step.bindings.output( outputId )
        assertNotNull( retrievedOutput )
        assertEquals( "my-output", retrievedOutput.spec.name )
        assertEquals( outputId, retrievedOutput.spec.id )
    }

    @Test
    fun `resolved input preserves source information`()
    {
        val stepId = UUID.randomUUID()
        val inputId = UUID.randomUUID()

        val inputBinding = createResolvedInput( id = inputId, name = "input-with-source" )

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to inputBinding )
        )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Step",
                version = Version( 1 )
            ),
            process = InTasksRun( "op" ),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        val retrieved = step.bindings.input( inputId )
        assertNotNull( retrieved )

        // Verify resolved source is preserved
        val source = retrieved.resolvedSource as? ResolvedDataSource.FileSystem
        assertNotNull( source )
        assertEquals( "/workspace/input/input.csv", source.resolvedPath )
    }

    @Test
    fun `resolved output preserves destination information`()
    {
        val stepId = UUID.randomUUID()
        val outputId = UUID.randomUUID()

        val outputBinding = createResolvedOutput( id = outputId, name = "output-with-dest" )

        val bindings = ResolvedBindings(
            outputs = mapOf( outputId to outputBinding )
        )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Step",
                version = Version( 1 )
            ),
            process = InTasksRun( "op" ),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        val retrieved = step.bindings.output( outputId )
        assertNotNull( retrieved )

        // Verify resolved destination is preserved
        val destination = retrieved.resolvedDestination as? ResolvedDataDestination.File
        assertNotNull( destination )
        assertEquals( "/workspace/output/output.csv", destination.resolvedPath )
    }

    @Test
    fun `multiple inputs and outputs are preserved`()
    {
        val stepId = UUID.randomUUID()
        val input1Id = UUID.randomUUID()
        val input2Id = UUID.randomUUID()
        val output1Id = UUID.randomUUID()
        val output2Id = UUID.randomUUID()

        val input1 = createResolvedInput( id = input1Id, name = "input-1" )
        val input2 = createResolvedInput( id = input2Id, name = "input-2" )
        val output1 = createResolvedOutput( id = output1Id, name = "output-1" )
        val output2 = createResolvedOutput( id = output2Id, name = "output-2" )

        val bindings = ResolvedBindings(
            inputs = mapOf(
                input1Id to input1,
                input2Id to input2
            ),
            outputs = mapOf(
                output1Id to output1,
                output2Id to output2
            )
        )

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Step With Multiple Bindings",
                version = Version( 1 )
            ),
            process = InTasksRun( "op" ),
            bindings = bindings,
            environmentRef = UUID.randomUUID()
        )

        // Verify all inputs are accessible
        assertEquals( 2, step.bindings.inputs.size )
        assertEquals( "input-1", step.bindings.input( input1Id )?.spec?.name ?: "Missing")
        assertEquals( "input-2", step.bindings.input( input2Id )?.spec?.name ?: "Missing")

        // Verify all outputs are accessible
        assertEquals( 2, step.bindings.outputs.size )
        assertEquals( "output-1", step.bindings.output( output1Id )?.spec?.name ?: "Missing")
        assertEquals( "output-2", step.bindings.output( output2Id )?.spec?.name ?: "Missing")
    }

    @Test
    fun `command spec with multiple expanded arguments is preserved`()
    {
        val stepId = UUID.randomUUID()
        val dataRefId = UUID.randomUUID()
        val pathRefId = UUID.randomUUID()

        val step = PlannedStep(
            metadata = StepMetadata(
                id = stepId,
                name = "Complex Command",
                version = Version( 1 )
            ),
            process = CommandSpec(
                executable = "python",
                args = listOf(
                    ExpandedArg.Literal( "script.py" ),
                    ExpandedArg.Literal( "--output=/path" ),
                    ExpandedArg.DataReference( dataRefId ),
                    ExpandedArg.PathSubstitution( pathRefId, "--input=$()" ),
                    ExpandedArg.EnvironmentVariable( "MODEL_PATH", "--model=$()" )
                )
            ),
            bindings = ResolvedBindings(),
            environmentRef = UUID.randomUUID()
        )

        val encoded = CoreAnalyticsSerializer.json.encodeToString( step )
        val decoded = CoreAnalyticsSerializer.json.decodeFromString<PlannedStep>( encoded )

        assertEquals( step, decoded )

        // Verify argument count
        val processSpec = decoded.process as CommandSpec
        assertEquals( 5, processSpec.args.size )

        // Verify first argument
        assertIs<ExpandedArg.Literal>( processSpec.args[0] )
        assertEquals( "script.py", (processSpec.args[0] as ExpandedArg.Literal).value )

        // Verify DataReference is preserved
        val dataRef = processSpec.args[2] as ExpandedArg.DataReference
        assertEquals( dataRefId, dataRef.id )

        // Verify PathSubstitution is preserved
        val pathSub = processSpec.args[3] as ExpandedArg.PathSubstitution
        assertEquals( "--input=$()", pathSub.template )
        assertEquals( pathRefId, pathSub.id )

        // Verify EnvironmentVariable is preserved
        val envVar = processSpec.args[4] as ExpandedArg.EnvironmentVariable
        assertEquals( "MODEL_PATH", envVar.name )
        assertEquals( "--model=$()", envVar.template )
    }
}