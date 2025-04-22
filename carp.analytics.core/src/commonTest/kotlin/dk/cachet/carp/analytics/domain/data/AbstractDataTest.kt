package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AbstractDataTest {

    @Test
    fun validateNameShouldThrowOnEmptyName() {
        val data = TestData("", "validType", DataLocation(listOf("data.csv")))
        val exception = assertFailsWith<IllegalArgumentException> { data.validateName() }
        assertEquals("Name cannot be empty", exception.message)
    }

    @Test
    fun validateDataTypeShouldThrowOnEmptyType() {
        val data = TestData("ValidName", "", DataLocation(listOf("data.csv")))
        val exception = assertFailsWith<IllegalArgumentException> { data.validateDataType() }
        assertEquals("DataType cannot be empty", exception.message)
    }

    @Test
    fun validateDataLocationShouldThrowOnEmptyPath() {
        val data = TestData("ValidName", "ValidType", DataLocation(emptyList()))
        val exception = assertFailsWith<IllegalArgumentException> { data.validateDataLocation() }
        assertEquals("Path cannot be empty", exception.message)
    }

    @Test
    fun allValidationPassesOnValidInput() {
        val data = TestData("OK", "type", DataLocation(listOf("test.csv")))
        assertTrue {
            data.validateName()
            data.validateDataType()
            data.validateDataLocation()
            true
        }
    }

    private class TestData(
        override val name: String,
        override val dataType: String,
        override val dataLocation: DataLocation
    ) : AbstractData(name, dataType, dataLocation)
}
