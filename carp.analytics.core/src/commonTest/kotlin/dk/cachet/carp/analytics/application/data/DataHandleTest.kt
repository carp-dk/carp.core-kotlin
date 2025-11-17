package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.ICarpTabularData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DataHandleTest
{
    // Simple mock implementation of ICarpTabularData
    private class MockTabularData : ICarpTabularData

    @Test
    fun `in memory data holds dataset`()
    {
        val mockData = MockTabularData()
        val handle = InMemoryData(mockData)

        // Verify the handle contains the expected data
        assertSame(mockData, handle.dataset, "Should return the same dataset instance")
    }

    @Test
    fun `file data holds path and mimetype`()
    {
        val file = FileData("/tmp/test.csv", "text/csv")
        assertEquals("/tmp/test.csv", file.path)
        assertEquals("text/csv", file.mimeType)
    }
}
