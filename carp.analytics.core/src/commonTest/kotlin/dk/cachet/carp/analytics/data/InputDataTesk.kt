package dk.cachet.carp.analytics.domain.data

import kotlin.test.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InputDataTest {

    @Test
    fun testValidInputDataInitialization() {
        val location = DataLocation(listOf("input", "path"))
        val inputData = InputDataReference("InputName", "Int", location)

        assertEquals("InputName", inputData.name)
        assertEquals("Int", inputData.dataType)
        assertEquals(location, inputData.source)
    }

    @Test
    fun testInputDataWithEmptyName() {
        val location = DataLocation(listOf("input", "path"))
        val exception = assertFailsWith<IllegalArgumentException> {
            InputDataReference("", "Int", location)
        }
        assertTrue(exception.message!!.contains("Name cannot be empty"))
    }

    @Test
    fun testInputDataWithEmptyDataType() {
        val location = DataLocation(listOf("input", "path"))
        val exception = assertFailsWith<IllegalArgumentException> {
            InputDataReference("InputName", "", location)
        }
        assertTrue(exception.message!!.contains("DataType cannot be empty"))
    }

    @Test
    fun testInputDataWithEmptyDataLocation() {
        val exception = assertFailsWith<IllegalArgumentException> {
            InputDataReference("InputName", "Int", DataLocation(emptyList()))
        }
        assertTrue(exception.message!!.contains("Input data source path cannot be empty"))
    }

    @Test
    fun canSerializeAndDeserializeInputData() {
        val original = InputDataReference("foo", "String", DataLocation(listOf("input", "file.csv")))
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<InputDataReference>(json)

        assertEquals(original, restored)
    }

}
