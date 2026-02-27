package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.ICarpTabularData
import dk.cachet.carp.analytics.domain.data.InMemorySource
import kotlin.test.*

class DataRegistryTest
{
    // Simple mock implementation of ICarpTabularData
    private class MockTabularData : ICarpTabularData

    private fun createDummyDataSet(): ICarpTabularData = MockTabularData()

    @Test
    fun testRegisterAndResolveData()
    {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet))

        val resolved = registry.resolve("input")
        assertTrue(resolved is InMemoryData)
        assertEquals(dataSet, (resolved).dataset)
    }

    @Test
    fun testDuplicateRegistrationThrows()
    {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet))

        assertFailsWith<IllegalArgumentException> {
            registry.register("input", InMemoryData(dataSet))
        }
    }

    @Test
    fun testResolveNonexistentThrows()
    {
        val registry = DataRegistry()

        assertFailsWith<IllegalArgumentException> {
            registry.resolve("missing_data")
        }
    }

    @Test
    fun testIsRegisteredWorks()
    {
        val registry = DataRegistry()
        val dataSet = createDummyDataSet()

        assertFalse(registry.isRegistered("input"))

        registry.register("input", InMemoryData(dataSet))

        assertTrue(registry.isRegistered("input"))
    }

    @Test
    fun testOverwriteWorks()
    {
        val registry = DataRegistry()
        val dataSet1 = createDummyDataSet()
        val dataSet2 = createDummyDataSet()

        registry.register("input", InMemoryData(dataSet1))
        registry.overwrite("input", InMemoryData(dataSet2))

        val resolved = registry.resolve("input")
        assertTrue(resolved is InMemoryData)
        assertEquals(dataSet2, (resolved).dataset)
    }
}
