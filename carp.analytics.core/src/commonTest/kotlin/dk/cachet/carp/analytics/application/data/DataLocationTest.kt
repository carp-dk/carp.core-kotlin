package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DataLocationTest {

    @Test
    fun canSerializeAndDeserializeDataLocation() {
        val original = DataLocation(listOf("some", "folder", "file.csv"), true, "file")
        val json = Json.encodeToString(original)
        val restored = Json.decodeFromString<DataLocation>(json)

        assertEquals(original, restored)
    }

    @Test
    fun asPosixPathProducesCorrectString() {
        val location = DataLocation(listOf("data", "input.csv"), isAbsolute = true)
        assertEquals("/data/input.csv", location.asPosixPath())

        val relative = DataLocation(listOf("temp", "output.csv"), isAbsolute = false)
        assertEquals("temp/output.csv", relative.asPosixPath())
    }

    @Test
    fun testValidDataLocationCreation() {
        val location = DataLocation(listOf("db", "path"), isAbsolute = true, scheme = "database")

        assertEquals("database", location.scheme)
        assertTrue(location.isAbsolute)
        assertEquals(listOf("db", "path"), location.segments)
        assertEquals("/db/path", location.asPosixPath())
    }

    @Test
    fun testInvalidDataLocationPath() {
        val exception = assertFailsWith<IllegalArgumentException> {
            validateDataLocation(DataLocation(emptyList()))
        }
        assertTrue(exception.message!!.contains("Path cannot be empty"))
    }

    private fun validateDataLocation(location: DataLocation) {
        require(location.segments.isNotEmpty()) { "Path cannot be empty" }
    }
}
