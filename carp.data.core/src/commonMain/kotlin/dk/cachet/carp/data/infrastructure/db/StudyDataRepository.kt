package dk.cachet.carp.data.infrastructure.db

import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

/**
 * Repository interface for querying collected study data from a database.
 */
interface StudyDataRepository
{
    /**
     * Query collected data points based on study and optional filters.
     *
     * @param studyId The ID of the study to query (required).
     * @param subjectDeploymentIds Optional filter: only return data for these subject IDs.
     * @param deviceRoleNames Optional filter: only return data collected from specific devices.
     * @param fields Optional filter: only return specific data types/fields.
     * @param from Optional start time filter (inclusive).
     * @param to Optional end time filter (inclusive).
     * @param offsetDays Optional time window after enrollment (e.g., first 30 days).
     *
     * @return A list of matching [CollectedDataPoint]s.
     */
    suspend fun queryData(
        studyId: UUID,
        subjectDeploymentIds: Set<UUID>? = null,
        deviceRoleNames: Set<String>? = null,
        fields: Set<String>? = null, // Here "fields" = data_type_name
        from: Instant? = null,
        to: Instant? = null,
        offsetDays: Int? = null
    ): List<CollectedDataPoint>
}
