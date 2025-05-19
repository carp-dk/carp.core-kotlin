package dk.cachet.carp.data.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.ApiResponse
import dk.cachet.carp.common.infrastructure.versioning.ApplicationServiceApiMigrator
import dk.cachet.carp.common.infrastructure.versioning.Major1Minor0To1Migration
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.data.infrastructure.StudyDataServiceInvoker
import dk.cachet.carp.data.infrastructure.StudyDataServiceRequest
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

val StudyDataServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyDataService.API_VERSION,
    StudyDataServiceInvoker,
    StudyDataServiceRequest.Serializer,
    StudyDataService.Event.serializer(),
    emptyList() // or add a migration object like major1Minor0To1Migration if needed
)
