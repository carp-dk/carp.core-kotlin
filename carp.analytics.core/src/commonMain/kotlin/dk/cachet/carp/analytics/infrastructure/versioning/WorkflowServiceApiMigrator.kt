package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.analytics.application.WorkflowService
import dk.cachet.carp.analytics.infrastructure.WorkflowServiceInvoker
import dk.cachet.carp.analytics.infrastructure.WorkflowServiceRequest
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator


val WorkflowServiceApiMigrator = ApplicationServiceApiMigrator(
    WorkflowService.API_VERSION,
    WorkflowServiceInvoker,
    WorkflowServiceRequest.Serializer,
    WorkflowService.Event.serializer(),
    emptyList() // or add a migration object like major1Minor0To1Migration if needed
)
