package dk.cachet.carp.analytics.application.plan

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolvedBindingsTest
{

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = false
    }

    @Test
    fun `ResolvedBindings serializes and deserializes`()
    {
        val inputId = UUID.randomUUID()
        val outputId = UUID.randomUUID()
        val dataRefId = UUID.randomUUID()
        val dataSinkRefId = UUID.randomUUID()
        val bindings = ResolvedBindings(
            inputs = mapOf(inputId to DataRef(dataRefId, "text/plain")),
            outputs = mapOf(outputId to DataRef(dataSinkRefId, "text/plain"))
        )

        val encoded = json.encodeToString(bindings)
        val decoded = json.decodeFromString<ResolvedBindings>(encoded)

        assertEquals(bindings, decoded)
    }


    @Test
    fun `DataRef and DataSinkRef validate fields`()
    {
        val validId = UUID.randomUUID()
        // DataRef should work with valid UUID and type
        DataRef(validId, "valid-type")
        DataRef(validId, "valid-type")

        assertFailsWith<IllegalArgumentException> { DataRef(validId, "") }
        assertFailsWith<IllegalArgumentException> { DataRef(validId, " ") }
        assertFailsWith<IllegalArgumentException> { DataRef(validId, "") }
        assertFailsWith<IllegalArgumentException> { DataRef(validId, " ") }
    }
}
