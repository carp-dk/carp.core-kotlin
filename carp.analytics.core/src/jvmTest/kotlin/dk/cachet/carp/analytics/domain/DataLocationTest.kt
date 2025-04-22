package dk.cachet.carp.analytics.domain

import dk.cachet.carp.analytics.domain.data.DataLocation
import dk.cachet.carp.analytics.domain.data.DataSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DataLocationTest {

    @Test
    fun `asPosixPath returns correct path`() {
        val location = DataLocation(listOf("data", "input.csv"), isAbsolute = true)
        assertEquals("/data/input.csv", location.asPosixPath())
    }

    @Test
    fun `asPosixPath handles relative paths`() {
        val location = DataLocation(listOf("temp", "output.csv"), isAbsolute = false)
        assertEquals("temp/output.csv", location.asPosixPath())
    }

}
