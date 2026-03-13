package dk.cachet.carp.analytics.application.execution

import kotlin.test.Test
import kotlin.test.assertEquals

class ContentTypeDetectorTest
{

    private val detector: ContentTypeDetector = ExtensionBasedContentTypeDetector

    @Test
    fun detectsJsonByExtension()
    {
        val result = detector.detect("data.json")
        assertEquals("application/json", result)
    }

    @Test
    fun detectsCsvByExtension()
    {
        val result = detector.detect("data.csv")
        assertEquals("text/csv", result)
    }

    @Test
    fun detectsTextByExtension()
    {
        val result = detector.detect("log.txt")
        assertEquals("text/plain", result)
    }

    @Test
    fun prefersDeclaredType()
    {
        val result = detector.detect("data.csv", "text/custom")
        assertEquals("text/custom", result)
    }

    @Test
    fun ignoresDeclaredTypeIfNotMimeType()
    {
        val result = detector.detect("data.csv", "edf") // Not a MIME type
        assertEquals("text/csv", result)
    }

    @Test
    fun fallsBackToOctetStream()
    {
        val result = detector.detect("unknown.xyz")
        assertEquals("application/octet-stream", result)
    }

    @Test
    fun handlesCaseInsensitive()
    {
        val result = detector.detect("DATA.JSON")
        assertEquals("application/json", result)
    }
}
