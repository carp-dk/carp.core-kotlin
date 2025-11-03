package dk.cachet.carp.analytics.domain.data

import kotlinx.serialization.Serializable

@Serializable
data class DataLocation(
    val segments: List<String>, // e.g., ["data", "inputs", "input.csv"]
    val isAbsolute: Boolean = true,
    val scheme: String = "file" // e.g., "file", "http", "s3"
)
{
    fun asPosixPath(): String =
        (if (isAbsolute) "/" else "") + segments.joinToString("/")
}
