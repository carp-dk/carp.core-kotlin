package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.analytics.application.ScheduleManagementService
import dk.cachet.carp.analytics.infrastructure.ScheduleManagementServiceInvoker
import dk.cachet.carp.analytics.infrastructure.ScheduleManagementServiceRequest
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import kotlinx.serialization.json.JsonObject

private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration() {
        override fun migrateRequest(request: JsonObject) = request.migrate { }

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ) = response

        override fun migrateEvent(event: JsonObject) = event.migrate { }
    }

val ScheduleManagementServiceApiMigrator = ApplicationServiceApiMigrator(
    ScheduleManagementService.API_VERSION,
    ScheduleManagementServiceInvoker,
    ScheduleManagementServiceRequest.Serializer,
    ScheduleManagementService.Event.serializer(),
    emptyList()
)
