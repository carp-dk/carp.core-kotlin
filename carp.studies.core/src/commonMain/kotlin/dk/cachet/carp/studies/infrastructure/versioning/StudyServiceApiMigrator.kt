package dk.cachet.carp.studies.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.versioning.*
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.infrastructure.StudyServiceInvoker
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*


private val major1Minor0To1Migration =
    object : Major1Minor0To1Migration()
    {
        override fun migrateRequest( request: JsonObject ) = request.migrate {
            ifType( "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.SetProtocol" ) {
                addVersionField( "protocol" )
            }
        }

        override fun migrateResponse( request: JsonObject, response: ApiResponse, targetVersion: ApiVersion ) =
            when ( request.getType() )
            {
                // Remove new field from `StudyProtocolSnapshot`.
                "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.GetStudyDetails" ->
                {
                    val responseObject = (response.response as? JsonObject)
                        ?.migrate { removeVersionField( "protocolSnapshot" ) }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }

        override fun migrateEvent( event: JsonObject ) = event.migrate {
            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyCreated" ) {
                updateObject( "study" )
                {
                    val protocolField = "protocolSnapshot"
                    if ( json[ protocolField ] != JsonNull ) addVersionField( protocolField )
                }
            }
            ifType( "dk.cachet.carp.studies.application.StudyService.Event.StudyGoneLive" ) {
                updateObject( "study" )
                {
                    val protocolField = "protocolSnapshot"
                    if ( json[ protocolField ] != JsonNull ) addVersionField( protocolField )
                }
            }
        }
    }

@Suppress( "MagicNumber" )
private val major1Minor1to3Migration =
    object : ApiMigration( 1, 3 )
    {
        override fun migrateRequest( request: JsonObject ): JsonObject = request

        override fun migrateResponse(
            request: JsonObject,
            response: ApiResponse,
            targetVersion: ApiVersion
        ): ApiResponse =
            when ( request.getType( ) )
            {
                "dk.cachet.carp.studies.infrastructure.StudyServiceRequest.GetStudyDetails" ->
                {
                    val responseObject = ( response.response as? JsonObject )
                        ?.migrate {
                            json.remove( "protocolVersion" )
                        }
                    ApiResponse( responseObject, response.ex )
                }
                else -> response
            }
        override fun migrateEvent( event: JsonObject ) = event.migrate {
            ifType("dk.cachet.carp.studies.application.StudyService.Event.StudyCreated") {
                addProtocolSnapshotVersion( "study" )
            }
            ifType("dk.cachet.carp.studies.application.StudyService.Event.StudyGoneLive") {
                addProtocolSnapshotVersion( "study" )
            }
        }
        /**
         * The `protocolVersion` field was added to the `StudyProtocolSnapshot` object.
         * This allows old StudyService run with a newer RecruitmentService
         */
        fun ApiJsonObjectMigrationBuilder.addProtocolSnapshotVersion( fieldName: String ) =
            updateObject( fieldName )
            {
                val newField = "protocolVersion"
                val initialVersion = ProtocolVersion( "Initial", Clock.System.now() )
                val protocolVersionJson = JSON.encodeToJsonElement( ProtocolVersion.serializer(), initialVersion )
                if ( json[ newField ] == JsonNull ) json[ newField ] = protocolVersionJson
            }
    }

val StudyServiceApiMigrator = ApplicationServiceApiMigrator(
    StudyService.API_VERSION,
    StudyServiceInvoker,
    StudyServiceRequest.Serializer,
    StudyService.Event.serializer(),
    listOf( major1Minor0To1Migration, major1Minor1to3Migration )
)
