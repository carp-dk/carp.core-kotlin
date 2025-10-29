package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.OutputDataReference
import kotlinx.serialization.json.Json
import kotlin.test.*

class OutputDataTest
{

    @Test
    fun testValidOutputDataInitialization()
    {
        val location = DataLocation(listOf("output", "path"))
        val outputData = OutputDataReference("OutputName", "Float", location)

        assertEquals("OutputName", outputData.name)
        assertEquals("Float", outputData.dataType)
        assertEquals(location, outputData.destination)
    }

    @Test
    fun testOutputDataWithEmptyName()
    {
        val location = DataLocation(listOf("output", "path"))
        val exception = assertFailsWith<IllegalArgumentException>
        {
            OutputDataReference("", "Float", location)
        }
        assertTrue(exception.message!!.contains("Name cannot be empty"))
    }

    @Test
    fun testOutputDataWithEmptyDataType()
    {
        val location = DataLocation(listOf("output", "path"))
        val exception = assertFailsWith<IllegalArgumentException>
        {
            OutputDataReference("OutputName", "", location)
        }
        assertTrue(exception.message!!.contains("DataType cannot be empty"))
    }

    @Test
    fun testOutputDataWithEmptyDataLocation()
    {
        val exception = assertFailsWith<IllegalArgumentException>
        {
            OutputDataReference("OutputName", "Float", DataLocation(emptyList()))
        }
        assertTrue(exception.message!!.contains("Output data source path cannot be empty"))
    }

    @Test
    fun canSerializeAndDeserializeOutputData()
    {
        val original = OutputDataReference("foo", "String", DataLocation(listOf("input", "file.csv")))
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<OutputDataReference>(json)

        assertEquals(original, restored)
    }
}
