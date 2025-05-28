package dk.cachet.carp.data.infrastructure.db

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Instant

/**
 * A [StudyDataService] implementation backed by a [StudyDataRepository].
 */
class DBBackedStudyDataService(
    private val repository: StudyDataRepository
) : StudyDataService {

    override suspend fun getCollectedData(
        studyId: UUID,
        studyDeploymentIds: Set<UUID>?,
        deviceRoleNames: Set<String>?,
        fields: Set<String>?,
        from: Instant?,
        to: Instant?,
        offsetDays: Int?
    ): CollectedDataSet {

        val collectedDataPoints: List<CollectedDataPoint> = repository.queryData(
            studyId = studyId,
            subjectDeploymentIds = studyDeploymentIds,
            deviceRoleNames = deviceRoleNames,
            dataTypeNames = fields,
            from = from,
            to = to,
            offsetDays = offsetDays
        )

        return CollectedDataSet(collectedDataPoints)
    }
}
