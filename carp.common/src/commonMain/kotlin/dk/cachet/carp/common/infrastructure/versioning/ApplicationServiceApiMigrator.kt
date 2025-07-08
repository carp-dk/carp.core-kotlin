package dk.cachet.carp.common.infrastructure.versioning

import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive


/**
 * Supports transforming between different API versions of [ApplicationServiceRequest] and [IntegrationEvent] objects.
 */
class ApplicationServiceApiMigrator<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    val runtimeVersion: ApiVersion,
    val requestInvoker: ApplicationServiceInvoker<TService, TRequest>,
    val requestObjectSerializer: KSerializer<out TRequest>,
    val eventSerializer: KSerializer<out IntegrationEvent<TService>>,
    migrations: List<ApiMigration> = emptyList()
)
{
    // Sort migrations and throw exception if there are missing or conflicting migrations.
    private val migrations = migrations.sortedBy { it.minimumMinorVersion }.also {
        val targetVersion = runtimeVersion.minor

        val isRangeCovered =
            if ( it.isEmpty() ) targetVersion == 0
            else it.first().minimumMinorVersion == 0 && it.last().targetMinorVersion == targetVersion

        var curVersion = 0
        val noGapsOrConflicts = it.fold( true ) { isValid, migration ->
            val curValid = isValid && migration.minimumMinorVersion == curVersion
            curVersion = migration.targetMinorVersion
            curValid
        }

        require( isRangeCovered && noGapsOrConflicts ) { "There are missing or conflicting migrations." }
    }

    /**
     * Migrate the [request] so that it matches [runtimeVersion] and deserialize using [json].
     *
     * @throws IllegalArgumentException if:
     *  - [request] does not contain an [ApiVersion]
     *  - the [request] version is more recent than the runtime version
     *  - the runtime version is a later major version than the [request] version
     */
    fun migrateRequest( json: Json, request: JsonObject ): MigratedRequest<TService, TRequest>
    {
        val requestVersion = getAndValidateApiVersion( request )

        // Apply request migrations.
        val toApply = migrations.dropWhile { requestVersion.minor >= it.targetMinorVersion }
        val updatedRequest = toApply.fold( request ) { r, migration -> migration.migrateRequest( r ) }

        // Defer applying response migrations.
        fun downgradeResponse( response: JsonElement?, ex: Exception? ): JsonElement
        {
            val updatedResponse = toApply.reversed().fold( ApiResponse( response, ex ) ) { curResponse, migration ->
                migration.migrateResponse( request, curResponse, requestVersion )
            }

            return if ( updatedResponse.ex != null ) throw updatedResponse.ex
                else updatedResponse.response!!
        }

        val decodedRequest = json.decodeFromJsonElement( requestObjectSerializer, updatedRequest )
        return MigratedRequest( json, decodedRequest, requestInvoker, ::downgradeResponse )
    }

    /**
     * Migrate the [event] so that it matches [runtimeVersion] and deserialize using [json].
     *
     * @throws IllegalArgumentException if:
     *  - [event] does not contain an [ApiVersion]
     *  - the [event] version is more recent than the runtime version
     *  - the runtime version is a later major version than the [event] version
     */
    fun migrateEvent( json: Json, event: JsonObject ): IntegrationEvent<TService>
    {
        val eventVersion = getAndValidateApiVersion( event )
        val toApply = migrations.dropWhile { eventVersion.minor >= it.targetMinorVersion }
        val updatedEvent = toApply.fold( event ) { e, migration -> migration.migrateEvent( e ) }

        return json.decodeFromJsonElement( eventSerializer, updatedEvent )
    }

    private fun getAndValidateApiVersion( jsonObject: JsonObject ): ApiVersion
    {
        val requestVersionString = jsonObject[ API_VERSION_FIELD ]?.jsonPrimitive?.content
        requireNotNull( requestVersionString )
            { "Object needs to contain `apiVersion` for migration to succeed." }

        val requestVersion = ApiVersion.fromString( requestVersionString )
        require( !requestVersion.isMoreRecent( runtimeVersion ) )
            { "Object version ($requestVersion) is more recent than the runtime version ($runtimeVersion)." }
        require( runtimeVersion.major == requestVersion.major )
            { "Can't migrate to new major versions." }

        return requestVersion
    }
}


/**
 * A [request] which can be invoked using [invokeOn] which will return the response expected by the version of the caller.
 */
class MigratedRequest<
    TService : ApplicationService<TService, *>,
    TRequest : ApplicationServiceRequest<TService, *>
>(
    val json: Json,
    val request: TRequest,
    private val requestInvoker: ApplicationServiceInvoker<TService, TRequest>,
    private val downgradeResponse: (JsonElement?, Exception?) -> JsonElement
)
{
    /**
     * Invoke this request on [service] and convert the response to the version expected by the original caller.
     */
    suspend fun invokeOn( service: TService ): JsonElement
    {
        @Suppress( "TooGenericExceptionCaught" )
        val response =
            try { requestInvoker.invokeOnService( request, service ) }
            catch ( ex: Exception ) { return downgradeResponse( null, ex ) }

        @Suppress( "UNCHECKED_CAST" )
        val responseJson = json.encodeToJsonElement( request.getResponseSerializer() as KSerializer<Any?>, response )
        return downgradeResponse( responseJson, null )
    }
}
