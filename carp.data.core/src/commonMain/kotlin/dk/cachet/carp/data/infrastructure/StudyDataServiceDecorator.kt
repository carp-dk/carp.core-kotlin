package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.Command
import dk.cachet.carp.data.application.CollectedDataQuery
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.StudyDataService

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
        query: CollectedDataQuery
    ): CollectedDataSet =
        invoke(
            StudyDataServiceRequest.GetCollectedData(
                studyId,
                query
            )
        )
}
