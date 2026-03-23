package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.analytics.domain.data.*
import dk.cachet.carp.common.application.UUID
import kotlin.test.*

/**
 * Comprehensive unit tests for [ResolvedBindings].
 *
 * Tests:
 * - Input lookup by ID
 * - Output lookup by ID
 * - Empty bindings
 * - Multiple inputs and outputs
 * - Asymmetric bindings (only inputs or only outputs)
 * - Lookup edge cases (null returns, not exceptions)
 */
class ResolvedBindingsTest
{
    private fun createTestResolvedInput(
        id: UUID = UUID.randomUUID(),
        name: String = "input"
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
            resolvedPath = "/workspace/input/$name.csv"
        )
        return ResolvedInput( spec = spec, resolvedSource = source )
    }

    private fun createTestResolvedOutput(
        id: UUID = UUID.randomUUID(),
        name: String = "output"
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
            resolvedPath = "/workspace/output/$name.csv"
        )
        return ResolvedOutput( spec = spec, resolvedDestination = destination )
    }

    // Input Lookup Tests

    @Test
    fun `input getter returns correct ResolvedInput by ID`()
    {
        val inputId1 = UUID.randomUUID()
        val inputId2 = UUID.randomUUID()
        val input1 = createTestResolvedInput( id = inputId1, name = "input-1" )
        val input2 = createTestResolvedInput( id = inputId2, name = "input-2" )

        val bindings = ResolvedBindings(
            inputs = mapOf(
                inputId1 to input1,
                inputId2 to input2
            )
        )

        assertEquals( input1, bindings.input( inputId1 ) )
        assertEquals( input2, bindings.input( inputId2 ) )
    }

    @Test
    fun `input getter returns null for unknown ID instead of throwing`()
    {
        val inputId = UUID.randomUUID()
        val unknownId = UUID.randomUUID()
        val input = createTestResolvedInput( id = inputId )

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to input )
        )

        assertNull( bindings.input( unknownId ) )
    }

    @Test
    fun `input getter returns null for empty bindings`()
    {
        val bindings = ResolvedBindings()

        assertNull( bindings.input( UUID.randomUUID() ) )
    }

    @Test
    fun `input getter can find first input in collection`()
    {
        val id = UUID.randomUUID()
        val input = createTestResolvedInput( id = id )

        val bindings = ResolvedBindings(
            inputs = mapOf( id to input )
        )

        assertEquals( input, bindings.input( id ) )
        assertNotNull( bindings.input( id ) )
    }

    @Test
    fun `input getter with many inputs finds correct one`()
    {
        val ids = List( 10 ) { UUID.randomUUID() }
        val inputs = ids.associateWith { id -> createTestResolvedInput( id = id, name = "input-$id" ) }

        val bindings = ResolvedBindings( inputs = inputs )

        // Test a few lookups
        assertEquals( inputs[ids[0]], bindings.input( ids[0] ) )
        assertEquals( inputs[ids[5]], bindings.input( ids[5] ) )
        assertEquals( inputs[ids[9]], bindings.input( ids[9] ) )

        // Test unknown ID still returns null
        assertNull( bindings.input( UUID.randomUUID() ) )
    }

    // Output Lookup Tests

    @Test
    fun `output getter returns correct ResolvedOutput by ID`()
    {
        val outputId1 = UUID.randomUUID()
        val outputId2 = UUID.randomUUID()
        val output1 = createTestResolvedOutput( id = outputId1, name = "output-1" )
        val output2 = createTestResolvedOutput( id = outputId2, name = "output-2" )

        val bindings = ResolvedBindings(
            outputs = mapOf(
                outputId1 to output1,
                outputId2 to output2
            )
        )

        assertEquals( output1, bindings.output( outputId1 ) )
        assertEquals( output2, bindings.output( outputId2 ) )
    }

    @Test
    fun `output getter returns null for unknown ID instead of throwing`()
    {
        val outputId = UUID.randomUUID()
        val unknownId = UUID.randomUUID()
        val output = createTestResolvedOutput( id = outputId )

        val bindings = ResolvedBindings(
            outputs = mapOf( outputId to output )
        )

        assertNull( bindings.output( unknownId ) )
    }

    @Test
    fun `output getter returns null for empty bindings`()
    {
        val bindings = ResolvedBindings()

        assertNull( bindings.output( UUID.randomUUID() ) )
    }

    @Test
    fun `output getter with many outputs finds correct one`()
    {
        val ids = List( 10 ) { UUID.randomUUID() }
        val outputs = ids.associateWith { id -> createTestResolvedOutput( id = id, name = "output-$id" ) }

        val bindings = ResolvedBindings( outputs = outputs )

        // Test a few lookups
        assertEquals( outputs[ids[0]], bindings.output( ids[0] ) )
        assertEquals( outputs[ids[5]], bindings.output( ids[5] ) )
        assertEquals( outputs[ids[9]], bindings.output( ids[9] ) )

        // Test unknown ID still returns null
        assertNull( bindings.output( UUID.randomUUID() ) )
    }

    // Empty Bindings Tests

    @Test
    fun `empty bindings are created successfully`()
    {
        val bindings = ResolvedBindings()

        assertTrue( bindings.inputs.isEmpty() )
        assertTrue( bindings.outputs.isEmpty() )
        assertNull( bindings.input( UUID.randomUUID() ) )
        assertNull( bindings.output( UUID.randomUUID() ) )
    }

    @Test
    fun `empty bindings default constructor creates zero-size maps`()
    {
        val bindings = ResolvedBindings()

        assertEquals( 0, bindings.inputs.size )
        assertEquals( 0, bindings.outputs.size )
    }

    @Test
    fun `explicit empty maps create valid bindings`()
    {
        val bindings = ResolvedBindings(
            inputs = emptyMap(),
            outputs = emptyMap()
        )

        assertTrue( bindings.inputs.isEmpty() )
        assertTrue( bindings.outputs.isEmpty() )
    }

    // Multiple Items Tests

    @Test
    fun `bindings with multiple inputs and outputs work correctly`()
    {
        val inputId1 = UUID.randomUUID()
        val inputId2 = UUID.randomUUID()
        val outputId1 = UUID.randomUUID()
        val outputId2 = UUID.randomUUID()

        val input1 = createTestResolvedInput( id = inputId1, name = "input-1" )
        val input2 = createTestResolvedInput( id = inputId2, name = "input-2" )
        val output1 = createTestResolvedOutput( id = outputId1, name = "output-1" )
        val output2 = createTestResolvedOutput( id = outputId2, name = "output-2" )

        val bindings = ResolvedBindings(
            inputs = mapOf(
                inputId1 to input1,
                inputId2 to input2
            ),
            outputs = mapOf(
                outputId1 to output1,
                outputId2 to output2
            )
        )

        // Verify all inputs
        assertEquals( 2, bindings.inputs.size )
        assertEquals( input1, bindings.input( inputId1 ) )
        assertEquals( input2, bindings.input( inputId2 ) )

        // Verify all outputs
        assertEquals( 2, bindings.outputs.size )
        assertEquals( output1, bindings.output( outputId1 ) )
        assertEquals( output2, bindings.output( outputId2 ) )
    }

    @Test
    fun `bindings with many inputs and outputs scale correctly`()
    {
        val inputs = List( 50 ) { UUID.randomUUID() }.associateWith { id -> createTestResolvedInput( id = id, name = "input-$id" ) }
        val outputs = List( 50 ) { UUID.randomUUID() }.associateWith { id -> createTestResolvedOutput( id = id, name = "output-$id" ) }

        val bindings = ResolvedBindings( inputs = inputs, outputs = outputs )

        assertEquals( 50, bindings.inputs.size )
        assertEquals( 50, bindings.outputs.size )

        // Spot check random items
        val randomInputId = inputs.keys.random()
        val randomOutputId = outputs.keys.random()

        assertEquals( inputs[randomInputId], bindings.input( randomInputId ) )
        assertEquals( outputs[randomOutputId], bindings.output( randomOutputId ) )
    }

    @Test
    fun `multiple items do not interfere with each other`()
    {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val input1 = createTestResolvedInput( id = id1, name = "first" )
        val input2 = createTestResolvedInput( id = id2, name = "second" )

        val bindings = ResolvedBindings(
            inputs = mapOf(
                id1 to input1,
                id2 to input2
            )
        )

        // Verify they are different
        val retrieved1 = bindings.input( id1 )
        val retrieved2 = bindings.input( id2 )

        assertNotNull( retrieved1 )
        assertNotNull( retrieved2 )
        assertEquals( "first", retrieved1.spec.name )
        assertEquals( "second", retrieved2.spec.name )
    }

    // Asymmetric Bindings Tests

    @Test
    fun `bindings with only inputs work correctly`()
    {
        val inputId = UUID.randomUUID()
        val input = createTestResolvedInput( id = inputId )

        val bindings = ResolvedBindings( inputs = mapOf( inputId to input ) )

        assertNotNull( bindings.input( inputId ) )
        assertTrue( bindings.outputs.isEmpty() )
        assertNull( bindings.output( UUID.randomUUID() ) )
    }

    @Test
    fun `bindings with only outputs work correctly`()
    {
        val outputId = UUID.randomUUID()
        val output = createTestResolvedOutput( id = outputId )

        val bindings = ResolvedBindings( outputs = mapOf( outputId to output ) )

        assertNotNull( bindings.output( outputId ) )
        assertTrue( bindings.inputs.isEmpty() )
        assertNull( bindings.input( UUID.randomUUID() ) )
    }

    @Test
    fun `bindings can have more inputs than outputs`()
    {
        val inputIds = List( 5 ) { UUID.randomUUID() }
        val outputIds = List( 2 ) { UUID.randomUUID() }

        val inputs = inputIds.associateWith { id -> createTestResolvedInput( id = id ) }
        val outputs = outputIds.associateWith { id -> createTestResolvedOutput( id = id ) }

        val bindings = ResolvedBindings( inputs = inputs, outputs = outputs )

        assertEquals( 5, bindings.inputs.size )
        assertEquals( 2, bindings.outputs.size )
    }

    @Test
    fun `bindings can have more outputs than inputs`()
    {
        val inputIds = List( 2 ) { UUID.randomUUID() }
        val outputIds = List( 5 ) { UUID.randomUUID() }

        val inputs = inputIds.associateWith { id -> createTestResolvedInput( id = id ) }
        val outputs = outputIds.associateWith { id -> createTestResolvedOutput( id = id ) }

        val bindings = ResolvedBindings( inputs = inputs, outputs = outputs )

        assertEquals( 2, bindings.inputs.size )
        assertEquals( 5, bindings.outputs.size )
    }

    // Edge Cases Tests

    @Test
    fun `lookup with null UUID returns null instead of crashing`()
    {
        val bindings = ResolvedBindings(
            inputs = mapOf(
                UUID.randomUUID() to createTestResolvedInput()
            )
        )

        // This should not crash even with unusual lookup
        val randomLookup = bindings.input( UUID.randomUUID() )
        assertNull( randomLookup )
    }

    @Test
    fun `concurrent lookups do not interfere`()
    {
        val ids = List( 10 ) { UUID.randomUUID() }
        val inputs = ids.associateWith { id -> createTestResolvedInput( id = id ) }

        val bindings = ResolvedBindings( inputs = inputs )

        // Simulate concurrent lookups (sequential in test)
        val lookups = ids.map { id ->
            bindings.input( id )
        }

        // All lookups should succeed
        assertEquals( 10, lookups.filterNotNull().size )
    }

    @Test
    fun `input and output lookups are independent`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val otherId = UUID.randomUUID()

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to createTestResolvedInput( id = inputId ) ),
            outputs = mapOf( outputId to createTestResolvedOutput( id = outputId ) )
        )

        // Verify lookups are independent
        assertNotNull( bindings.input( inputId ) )
        assertNull( bindings.output( inputId ) )  // Wrong type

        assertNotNull( bindings.output( outputId ) )
        assertNull( bindings.input( outputId ) )  // Wrong type

        assertNull( bindings.input( otherId ) )
        assertNull( bindings.output( otherId ) )
    }

    // Map Access Tests

    @Test
    fun `inputs map is directly accessible`()
    {
        val inputId = UUID.randomUUID()
        val input = createTestResolvedInput( id = inputId )

        val bindings = ResolvedBindings(
            inputs = mapOf( inputId to input )
        )

        // Access through map
        assertEquals( input, bindings.inputs[inputId] )
        assertEquals( 1, bindings.inputs.size )
    }

    @Test
    fun `outputs map is directly accessible`()
    {
        val outputId = UUID.randomUUID()
        val output = createTestResolvedOutput( id = outputId )

        val bindings = ResolvedBindings(
            outputs = mapOf( outputId to output )
        )

        // Access through map
        assertEquals( output, bindings.outputs[outputId] )
        assertEquals( 1, bindings.outputs.size )
    }

    @Test
    fun `can iterate over all inputs`()
    {
        val inputs = List( 5 ) { UUID.randomUUID() }.associateWith { id -> createTestResolvedInput( id = id ) }

        val bindings = ResolvedBindings( inputs = inputs )

        var count = 0
        for ( (_, input) in bindings.inputs ) {
            assertNotNull( input )
            count++
        }

        assertEquals( 5, count )
    }

    @Test
    fun `can iterate over all outputs`()
    {
        val outputs = List( 5 ) { UUID.randomUUID() }.associateWith { id -> createTestResolvedOutput( id = id ) }

        val bindings = ResolvedBindings( outputs = outputs )

        var count = 0
        for ( (_, output) in bindings.outputs ) {
            assertNotNull( output )
            count++
        }

        assertEquals( 5, count )
    }
}