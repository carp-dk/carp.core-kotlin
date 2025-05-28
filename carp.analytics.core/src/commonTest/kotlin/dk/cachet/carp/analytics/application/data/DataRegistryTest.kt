package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.StepCount
import dk.cachet.carp.data.application.DataStreamId
import kotlinx.datetime.Clock
import kotlin.test.*

class DataRegistryTest {

    private fun createDummyDataSet(): CollectedDataSet {
        val stepCount = StepCount(steps = 5000)
        val dataPoint = CollectedDataPoint(
            streamId = DataStreamId(
                studyDeploymentId = UUID.randomUUID(),
                deviceRoleName = "phone",
                dataType = NamespacedId("dk.cachet.carp", "step_count")
            ),
            data = stepCount,
            timestamp = Clock.System.now()
        )
        return CollectedDataSet(listOf(dataPoint))
    }

    @Test
    fun testRegisterAndResolveData() {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet))

        val resolved = registry.resolve("input")
        assertTrue(resolved is InMemoryData)
        assertEquals(dataSet, (resolved).dataset)
    }

    @Test
    fun testDuplicateRegistrationThrows() {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet))

        assertFailsWith<IllegalArgumentException> {
            registry.register("input", InMemoryData(dataSet))
        }
    }

    @Test
    fun testResolveNonexistentThrows() {
        val registry = DataRegistry()

        assertFailsWith<IllegalArgumentException> {
            registry.resolve("missing_data")
        }
    }

    @Test
    fun testIsRegisteredWorks() {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        assertFalse(registry.isRegistered("input"))

        registry.register("input", InMemoryData(dataSet))

        assertTrue(registry.isRegistered("input"))
    }

    @Test
    fun testOverwriteWorks() {
        val registry = DataRegistry()
        val dataSet1 = createDummyDataSet()
        val dataSet2 = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet1))
        registry.overwrite("input", InMemoryData(dataSet2))

        val resolved = registry.resolve("input")
        assertTrue(resolved is InMemoryData)
        assertEquals(dataSet2, (resolved).dataset)
    }

    @Test
    fun testToExecutionOutputsReturnsCorrectFormat() {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()
        registry.register("input", InMemoryData(dataSet))

        val outputs = registry.toExecutionOutputs()
        assertEquals(1, outputs.size)
        assertEquals("input", outputs[0].name)
        assertEquals("dataset", outputs[0].dataType)
        assertEquals("mem", outputs[0].location.scheme)
    }

    @Test
    fun testToArtifactsOnlyIncludesFiles() {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()
        registry.register("inmem", InMemoryData(dataSet))
        registry.register("output_file", FileData("/tmp/output.csv", "text/csv"))

        val artifacts = registry.toArtifacts()
        assertEquals(1, artifacts.size)
        assertEquals("output_file", artifacts[0].name)
        assertEquals("text/csv", artifacts[0].mimeType)
    }

}
