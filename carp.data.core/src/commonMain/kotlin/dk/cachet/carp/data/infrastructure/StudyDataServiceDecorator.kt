package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.data.application.CollectedDataSet
import kotlinx.datetime.Instant
import dk.cachet.carp.common.application.UUID

/**
 * Decorator which routes [StudyDataServiceRequest] commands to a [StudyDataService].
 */
class StudyDataServiceDecorator(
    service: StudyDataService,
    requestDecorator: (Command<StudyDataServiceRequest<*>>) -> Command<StudyDataServiceRequest<*>> = { it }
) : ApplicationServiceDecorator<StudyDataService, StudyDataServiceRequest<*>>(
    service,
    StudyDataServiceInvoker,
    requestDecorator
),
    StudyDataService
{
    override suspend fun getCollectedData(
        studyId: UUID,
        studyDeploymentIds: Set<UUID>?,
        deviceRoleName: Set<String>?,
        fields: Set<String>?,
        from: Instant?,
        to: Instant?,
        offsetDays: Int?
    ): CollectedDataSet =
        invoke(
            StudyDataServiceRequest.GetCollectedData(
                studyId,
                studyDeploymentIds,
                deviceRoleName,
                fields,
                from,
                to,
                offsetDays
            )
        )
}
