package dk.cachet.carp.analytics.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.analytics.application.ExecutionService
import dk.cachet.carp.analytics.infrastructure.ExecutionServiceInvoker
import dk.cachet.carp.analytics.infrastructure.ExecutionServiceRequest
import kotlinx.serialization.json.JsonObject


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration() {
        override fun migrateRequest(request: JsonObject) = request.migrate { }

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ) = response // no transformation needed

        override fun migrateEvent(event: JsonObject) = event.migrate { }
    }

val ExecutionServiceApiMigrator = ApplicationServiceApiMigrator(
    ExecutionService.API_VERSION,
    ExecutionServiceInvoker,
    ExecutionServiceRequest.Serializer,
    ExecutionService.Event.serializer(),
    emptyList() // or add a migration object like major1Minor0To1Migration if needed
)
