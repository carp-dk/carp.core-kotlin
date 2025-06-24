package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataQuery
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.*

class StudyDataServiceRequestTest
{

    private val studyId = UUID.randomUUID()
    private val fields = setOf("step_count", "heart_rate")
    private val now = Instant.parse("2025-04-27T00:00:00Z")

    @Test
    fun testSerializationRoundtrip()
    {
        val request = StudyDataServiceRequest.GetCollectedData(
            studyId = studyId,
            query = CollectedDataQuery(
                studyDeploymentIds = null,
                deviceRoleNames = null,
                fields = fields,
                from = now,
                to = null,
                offsetDays = null
            )
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(StudyDataServiceRequest.GetCollectedData.serializer(), request)
        val deserialized = json.decodeFromString(StudyDataServiceRequest.GetCollectedData.serializer(), serialized)

        assertEquals(request, deserialized)
    }

    @Test
    fun testMinimalSerialization()
    {
        val request = StudyDataServiceRequest.GetCollectedData(
            studyId = studyId,
            query = CollectedDataQuery(
                studyDeploymentIds = null,
                deviceRoleNames = null,
                fields = emptySet(),
                from = null,
                to = null,
                offsetDays = null
            )
        )

        val json = Json { prettyPrint = true }
        val serialized = json.encodeToString(StudyDataServiceRequest.GetCollectedData.serializer(), request)
        val deserialized = json.decodeFromString(StudyDataServiceRequest.GetCollectedData.serializer(), serialized)

        assertEquals(request, deserialized)
    }
}
