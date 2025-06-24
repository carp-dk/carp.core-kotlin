package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.serialization.json.Json
import kotlin.test.*

class StudyDataServiceRequestTest {

    private val studyId = UUID.randomUUID()
    private val subjectIds = setOf(UUID.randomUUID(), UUID.randomUUID())
    private val fields = setOf("step_count", "heart_rate")
    private val now = Instant.parse("2025-04-27T00:00:00Z")

    @Test
    fun testSerializationRoundtrip() {
        val request = StudyDataServiceRequest.GetCollectedData(
            studyId = studyId,
            studyDeploymentIds = subjectIds,
            fields = fields,
            from = now,
            to = now.plus(86400, DateTimeUnit.SECOND), // +1 day
            offsetDays = 30
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(StudyDataServiceRequest.GetCollectedData.serializer(), request)
        val deserialized = json.decodeFromString(StudyDataServiceRequest.GetCollectedData.serializer(), serialized)

        assertEquals(request, deserialized)
    }

    @Test
    fun testMinimalSerialization() {
        val request = StudyDataServiceRequest.GetCollectedData(
            studyId = studyId
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(StudyDataServiceRequest.GetCollectedData.serializer(), request)
        val deserialized = json.decodeFromString(StudyDataServiceRequest.GetCollectedData.serializer(), serialized)

        assertEquals(request, deserialized)
    }
}
