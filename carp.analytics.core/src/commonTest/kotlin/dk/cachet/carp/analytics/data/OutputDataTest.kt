package dk.cachet.carp.analytics.domain.data

import kotlin.test.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class OutputDataTest {

    @Test
    fun testValidOutputDataInitialization() {
        val location = DataLocation(listOf("output", "path"))
        val outputData = OutputData("OutputName", "Float", location)

        assertEquals("OutputName", outputData.name)
        assertEquals("Float", outputData.dataType)
        assertEquals(location, outputData.destination)
    }

    @Test
    fun testOutputDataWithEmptyName() {
        val location = DataLocation(listOf("output", "path"))
        val exception = assertFailsWith<IllegalArgumentException> {
            OutputData("", "Float", location)
        }
        assertTrue(exception.message!!.contains("Name cannot be empty"))
    }

    @Test
    fun testOutputDataWithEmptyDataType() {
        val location = DataLocation(listOf("output", "path"))
        val exception = assertFailsWith<IllegalArgumentException> {
            OutputData("OutputName", "", location)
        }
        assertTrue(exception.message!!.contains("DataType cannot be empty"))
    }

    @Test
    fun testOutputDataWithEmptyDataLocation() {
        val exception = assertFailsWith<IllegalArgumentException> {
            OutputData("OutputName", "Float", DataLocation(emptyList()))
        }
        assertTrue(exception.message!!.contains("Output data source path cannot be empty"))
    }

    @Test
    fun canSerializeAndDeserializeOutputData() {
        val original = OutputData("foo", "String", DataLocation(listOf("input", "file.csv")))
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<OutputData>(json)

        assertEquals(original, restored)
    }
}
