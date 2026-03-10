package dk.cachet.carp.analytics.application.execution

import kotlinx.serialization.Serializable

/**
 * A reference to a resource produced during step execution, such as stdout, stderr, or log files.
 *
 * The resource can be identified by a relative path or a URI,
 * and may include optional metadata such as media type and byte size.
 *
 * Note that the value is not a validated Path here since we wish to keep this class platform-agnostic.
 * The actual resolution and validation of the resource should be handled by the execution environment.
 */
@Serializable
data class ResourceRef(
    val kind: ResourceKind,
    val value: String,
    val mediaType: String? = null,
    val byteSize: Long? = null
)

@Serializable
enum class ResourceKind
{
    RELATIVE_PATH,
    URI
}
