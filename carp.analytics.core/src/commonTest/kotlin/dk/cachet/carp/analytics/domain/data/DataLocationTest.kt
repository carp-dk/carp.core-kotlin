package dk.cachet.carp.analytics.domain.data

import kotlin.test.Test
import kotlin.test.assertEquals

class DataLocationTest
{

    @Test
    fun `asPosixPath returns correct path`()
    {
        val location = DataLocation(listOf("data", "input.csv"), isAbsolute = true)
        assertEquals("/data/input.csv", location.asPosixPath())
    }

    @Test
    fun `asPosixPath handles relative paths`()
    {
        val location = DataLocation(listOf("temp", "output.csv"), isAbsolute = false)
        assertEquals("temp/output.csv", location.asPosixPath())
    }
}
