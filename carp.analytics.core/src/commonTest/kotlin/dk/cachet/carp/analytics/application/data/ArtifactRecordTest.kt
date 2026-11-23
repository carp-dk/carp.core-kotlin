package dk.cachet.carp.analytics.application.data

import dk.cachet.carp.common.application.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant


/**
 * Tests for [ArtifactRecord].
 */
class ArtifactRecordTest
{
    @Test
    fun can_initialize_cohort_artifact() { record( ArtifactScope.COHORT ) }

    @Test
    fun can_initialize_participant_artifact_without_deployment() { record( ArtifactScope.PARTICIPANT, "p-01" ) }

    @Test
    fun can_initialize_participant_artifact_with_deployment()
    {
        record( ArtifactScope.PARTICIPANT, "p-01", UUID.randomUUID() )
    }

    @Test
    fun cant_initialize_cohort_artifact_with_subject() =
        assertInvalid { record( ArtifactScope.COHORT, "p-01" ) }

    @Test
    fun cant_initialize_cohort_artifact_with_deployment() =
        assertInvalid { record( ArtifactScope.COHORT, deployment = UUID.randomUUID() ) }

    @Test
    fun cant_initialize_participant_artifact_without_subject() =
        assertInvalid { record( ArtifactScope.PARTICIPANT ) }


    private fun record( scope: ArtifactScope, subject: String? = null, deployment: UUID? = null ) =
        ArtifactRecord( UUID.randomUUID(), UUID.randomUUID(), REFERENCE, PRODUCED_AT, scope, subject, deployment )

    private fun assertInvalid( create: () -> ArtifactRecord )
    {
        assertFailsWith<IllegalArgumentException> { create() }
    }


    companion object
    {
        private val PRODUCED_AT = Instant.fromEpochMilliseconds( 1_700_000_000_000 )
        private val REFERENCE = ArtifactReference(
            "carp://artefacts/run-1/step-1/features.csv",
            "text/csv",
            10,
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
    }
}
