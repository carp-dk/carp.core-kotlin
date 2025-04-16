package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.domain.data.DataLocation

import java.nio.file.Path
import java.nio.file.Paths
// Path and Paths are used to make native JVM file path objects, no such objects exist in Kotlin. 
import kotlin.io.path.*
// Kotlin provides extension methods for Path and Paths to make it easier to work with file paths.
// These extension methods use idiomatic names such as readText() and readLines().

object DataLocationResolver {

    fun resolve(location: DataLocation): Path {
        require(location.scheme == "file") {
            "Only local file paths with scheme=file are supported on JVM"
        }
        return Paths.get(location.asPosixPath())
    }

    fun readAsText(location: DataLocation): String =
        resolve(location).readText()

    fun exists(location: DataLocation): Boolean =
        resolve(location).exists()

    fun readLines(location: DataLocation): List<String> =
        resolve(location).readLines()
}
