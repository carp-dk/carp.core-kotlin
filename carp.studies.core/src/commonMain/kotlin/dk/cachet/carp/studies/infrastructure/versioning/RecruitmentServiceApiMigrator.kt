package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.versioning.*
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceInvoker
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject


const val recruitmentRequest = "dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest"

private val major1Minor0To2Migration =
   object : ApiMigration( 0, 2 )
   {
       override fun migrateRequest( request: JsonObject ): JsonObject = request.migrate {
           ifType( "$recruitmentRequest.AddParticipant" )
           {
               changeType( "$recruitmentRequest.AddParticipantByEmailAddress" )
           }
       }

       override fun migrateResponse(
           request: JsonObject,
           response: ApiResponse,
           targetVersion: ApiVersion
       ): ApiResponse = response

       override fun migrateEvent( event: JsonObject ): JsonObject = event
   }


private val major1Minor2To3Migration =
    @Suppress( "MagicNumber" )
    object : ApiMigration( 2, 3 )
    {
        override fun migrateRequest( request: JsonObject ) = request

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
            when ( request.getType( ) )
            {
                "dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest.InviteNewParticipantGroup",
                "dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest.StopParticipantGroup" ->
                {
                    val responseObject = response.response?.jsonObject?.migrate {
                        removeDeviceRegistration()
                    }
                    ApiResponse( responseObject, response.ex )
                }
                "dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest.GetParticipantGroupStatusList" ->
                {
                    val responseObject = response.response?.jsonArray?.migrate {
                        objects { removeDeviceRegistration() }
                    }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        /**
         * Remove the newly added `deviceRegistration` field from `DeviceDeploymentStatus` objects contained within
         * `StudyDeploymentStatus`.
         */
        private fun ApiJsonObjectMigrationBuilder.removeDeviceRegistration() =
            updateObject( "studyDeploymentStatus" ) {
                updateArray( "deviceStatusList" ) {
                    objects { json.remove( "deviceRegistration" ) }
                }
            }

        override fun migrateEvent( event: JsonObject ) = event
    }


val RecruitmentServiceApiMigrator = ApplicationServiceApiMigrator(
    RecruitmentService.API_VERSION,
    RecruitmentServiceInvoker,
    RecruitmentServiceRequest.Serializer,
    RecruitmentService.Event.serializer(),
    listOf( major1Minor0To2Migration, major1Minor2To3Migration )
)
