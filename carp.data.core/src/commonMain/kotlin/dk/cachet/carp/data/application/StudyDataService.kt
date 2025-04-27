package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import kotlinx.datetime.Instant

/**
 * Provides researcher-optimized access to collected data for studies, devices, and streams.
 */
interface StudyDataService : ApplicationService<StudyDataService, Nothing>
{
    companion object { val API_VERSION = ApiVersion(1, 0) }

    /**
     * Retrieve collected data points based on study and optional filters.
     *
     * @param studyId The ID of the study to query (required).
     * @param studyDeploymentIds Optional list of subject deployment IDs to filter by.
     * @param fields Optional list of fields (relate to CARP Data Types) (e.g., "step_count") to include.
     * @param deviceRoleNames Optional list of device role names (e.g., "phone") to include.
     * @param from Optional absolute start time for filtering.
     * @param to Optional absolute end time for filtering.
     * @param offsetDays Optional relative offset window (e.g., first 30 days after subject enrollment).
     *
     * @return A [CollectedDataSet] containing matching data points.
     */
    suspend fun getCollectedData(
        studyId: UUID,
        studyDeploymentIds: Set<UUID>? = null,
        deviceRoleNames: Set<String>? = null,
        fields: Set<String>? = null,
        from: Instant? = null,
        to: Instant? = null,
        offsetDays: Int? = null
    ): CollectedDataSet
}
