package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.data.infrastructure.StudyDataServiceInvoker
import dk.cachet.carp.data.infrastructure.StudyDataServiceRequest

val StudyDataServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyDataService.API_VERSION,
    StudyDataServiceInvoker,
    StudyDataServiceRequest.Serializer,
    StudyDataService.Event.serializer(),
    emptyList()
)
