package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.analytics.application.ScheduleManagementService
import dk.cachet.carp.analytics.infrastructure.ScheduleManagementServiceInvoker
import dk.cachet.carp.analytics.infrastructure.ScheduleManagementServiceRequest
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator


val ScheduleManagementServiceApiMigrator = ApplicationServiceApiMigrator(
    ScheduleManagementService.API_VERSION,
    ScheduleManagementServiceInvoker,
    ScheduleManagementServiceRequest.Serializer,
    ScheduleManagementService.Event.serializer(),
    emptyList()
)
