package dk.cachet.carp.analytics.application.execution

/**
 * Determines content/MIME type based on file characteristics.
 *
 */
interface ContentTypeDetector
{
    /**
     * Detect content type.
     *
     * @param fileName The file name (for extension-based detection)
     * @param declaredType Optional declared type from output spec (takes precedence)
     * @return Detected or declared MIME type
     */
    fun detect( fileName: String, declaredType: String? = null ): String
}

/**
 * Extension-based content type detection.
 *
 * Maps file extensions to standard MIME types.
 * Falls back to application/octet-stream for unknown types.
 */
object ExtensionBasedContentTypeDetector : ContentTypeDetector
{
    override fun detect( fileName: String, declaredType: String? ): String
    {
        // Declared type takes precedence (if it's a MIME type, not just a label)
        if (!declaredType.isNullOrBlank() && declaredType.contains("/"))
        {
            return declaredType
        }

        // Fall back to extension-based detection
        val lowerName = fileName.lowercase()
        return EXTENSION_CONTENT_TYPES
            .entries
            .firstOrNull { (ext, _) -> lowerName.endsWith(ext) }
            ?.value
            ?: "application/octet-stream"
    }

    private val EXTENSION_CONTENT_TYPES = mapOf(
        ".json" to "application/json",
        ".xml" to "application/xml",
        ".csv" to "text/csv",
        ".tsv" to "text/tab-separated-values",
        ".txt" to "text/plain",
        ".log" to "text/plain",
        ".html" to "text/html",
        ".htm" to "text/html",
        ".pdf" to "application/pdf",
        ".zip" to "application/zip",
        ".gz" to "application/gzip",
        ".tar" to "application/x-tar",
        ".png" to "image/png",
        ".jpg" to "image/jpeg",
        ".jpeg" to "image/jpeg",
        ".gif" to "image/gif",
        ".svg" to "image/svg+xml",
        ".parquet" to "application/vnd.apache.parquet",
        ".avro" to "application/x-avro",
        ".protobuf" to "application/protobuf"
    )
}
