package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.infrastructure.ExecutionServiceInvoker
import dk.cachet.carp.analytics.infrastructure.ExecutionServiceRequest
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator

val ExecutionServiceApiMigrator = ApplicationServiceApiMigrator(
    ExecutionService.API_VERSION,
    ExecutionServiceInvoker,
    ExecutionServiceRequest.Serializer,
    ExecutionService.Event.serializer(),
    emptyList() // or add a migration object like major1Minor0To1Migration if needed
)
