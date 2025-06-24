package dk.cachet.carp.data.infrastructure

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.data.application.StudyDataService

/**
 * Invokes [StudyDataService] methods based on [StudyDataServiceRequest]s.
 */
object StudyDataServiceInvoker : ApplicationServiceInvoker<StudyDataService, StudyDataServiceRequest<*>>
{
    override suspend fun StudyDataServiceRequest<*>.invoke( service: StudyDataService ): Any =
        when (this)
        {
            is StudyDataServiceRequest.GetCollectedData ->
                service.getCollectedData(
                    studyId,
                    query
                )
        }
}
