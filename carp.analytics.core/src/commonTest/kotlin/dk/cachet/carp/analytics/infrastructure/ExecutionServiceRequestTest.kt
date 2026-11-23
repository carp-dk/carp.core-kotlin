package dk.cachet.carp.analytics.infrastructure

import dk.cachet.carp.analytics.infrastructure.serialization.CoreAnalyticsSerializer
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant


/**
 * Tests for [ExecutionServiceRequest].
 */
class ExecutionServiceRequestTest
{
    private val json = createDefaultJSON( CoreAnalyticsSerializer.module )

    @Test
    fun get_participant_results_request_keeps_since()
    {
        val request = ExecutionServiceRequest.GetParticipantResults(
            UUID.randomUUID(),
            Instant.fromEpochMilliseconds( 1_700_000_000_000 )
        )

        val serialized = json.encodeToString( ExecutionServiceRequest.Serializer, request )
        val parsed = json.decodeFromString( ExecutionServiceRequest.Serializer, serialized )

        assertEquals( request, parsed )
    }

    @Test
    fun get_participant_results_request_without_since()
    {
        val request = ExecutionServiceRequest.GetParticipantResults( UUID.randomUUID() )

        val serialized = json.encodeToString( ExecutionServiceRequest.Serializer, request )

        assertEquals( request, json.decodeFromString( ExecutionServiceRequest.Serializer, serialized ) )
    }
}
