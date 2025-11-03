package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.analytics.application.TriggerService
import dk.cachet.carp.analytics.infrastructure.TriggerServiceInvoker
import dk.cachet.carp.analytics.infrastructure.TriggerServiceRequest
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator


val TriggerServiceApiMigrator = ApplicationServiceApiMigrator(
    TriggerService.API_VERSION,
    TriggerServiceInvoker,
    TriggerServiceRequest.Serializer,
    TriggerService.Event.serializer(),
    emptyList()
)
