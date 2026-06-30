package dk.cachet.carp.protocols.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.infrastructure.serialization.ignoreTypeParameters
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.serialization.*
import kotlin.js.JsExport
import kotlin.time.Clock


/**
 * Serializable application service requests to [ProtocolService] which can be executed on demand.
 */
@Serializable
@JsExport
@Suppress( "NON_EXPORTABLE_TYPE" )
sealed class ProtocolServiceRequest<out TReturn> : ApplicationServiceRequest<ProtocolService, TReturn>()
{
    @Required
    override val apiVersion: ApiVersion = ProtocolService.API_VERSION

    object Serializer : KSerializer<ProtocolServiceRequest<*>> by ignoreTypeParameters( ::serializer )


    @Serializable
    data class Add( val protocol: StudyProtocolSnapshot, val versionTag: String = "Initial" ) :
        ProtocolServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
    }

    @Serializable
    data class AddVersion(
        val protocol: StudyProtocolSnapshot,
        val versionTag: String = Clock.System.now().toString()
    ) : ProtocolServiceRequest<Unit>()
    {
        override fun getResponseSerializer() = serializer<Unit>()
    }

    @Serializable
    data class UpdateParticipantDataConfiguration(
        val protocolId: UUID,
        val versionTag: String,
        val expectedParticipantData: Set<ExpectedParticipantData>
    ) : ProtocolServiceRequest<StudyProtocolSnapshot>()
    {
        override fun getResponseSerializer() = serializer<StudyProtocolSnapshot>()
    }

    @Serializable
    data class GetBy( val protocolId: UUID, val versionTag: String? = null ) :
        ProtocolServiceRequest<StudyProtocolSnapshot>()
    {
        override fun getResponseSerializer() = serializer<StudyProtocolSnapshot>()
    }

    @Serializable
    data class GetAllForOwner( val ownerId: UUID ) : ProtocolServiceRequest<List<StudyProtocolSnapshot>>()
    {
        override fun getResponseSerializer() = serializer<List<StudyProtocolSnapshot>>()
    }

    @Serializable
    data class GetVersionHistoryFor( val protocolId: UUID ) : ProtocolServiceRequest<List<ProtocolVersion>>()
    {
        override fun getResponseSerializer() = serializer<List<ProtocolVersion>>()
    }
}
