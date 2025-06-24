package dk.cachet.carp.data.infrastructure.db

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.data.application.CollectedDataQuery

/**
 * Repository interface for querying collected study data from a database.
 */
interface StudyDataRepository
{
/**
 * Query collected data points based on study and optional filters.
 *
 * @param studyId The ID of the study to query (required).
 * @param query The query object containing optional filters such as subjectDeploymentIds, deviceRoleNames, dataTypeNames, from, to, and offsetDays.
 * @return A list of matching [CollectedDataPoint]s.
 */
    suspend fun queryData(
        studyId: UUID,
        query: CollectedDataQuery
    ): List<CollectedDataPoint>
}
