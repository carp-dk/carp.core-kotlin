package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * Provides researcher-optimized access to collected data for studies, devices, and streams.
 */
interface StudyDataService : ApplicationService<StudyDataService, StudyDataService.Event>
{

    companion object
    {
        val API_VERSION = ApiVersion(1, 0)
    }

    @Serializable
    sealed class Event : IntegrationEvent<StudyDataService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }

/**
 * Retrieves collected data points for a study, filtered by optional parameters.
 *
 * @param studyId The unique identifier of the study to query. (Required)
 * @param query Optional filters for the data query, including:
 *   - studyDeploymentIds: List of subject deployment IDs to filter by.
 *   - fields: List of CARP Data Type fields (e.g., `step_count`) to include.
 *   - deviceRoleNames: List of device role names (e.g., `phone`) to include.
 *   - from: Absolute start time for filtering.
 *   - to: Absolute end time for filtering.
 *   - offsetDays: Relative offset window (e.g., first 30 days after subject enrollment).
 *
 * @return A [CollectedDataSet] containing the matching data points.
 */
    suspend fun getCollectedData(
        studyId: UUID,
        query: CollectedDataQuery
    ): CollectedDataSet
}
