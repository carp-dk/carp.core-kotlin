package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.serialization.*
import kotlin.time.Clock


/**
 * Application service which allows managing (multiple versions of) [StudyProtocolSnapshot]'s,
 * which can be instantiated locally through [StudyProtocol].
 */
interface ProtocolService : ApplicationService<ProtocolService, ProtocolService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 1 ) }

    @Serializable
    sealed class Event : IntegrationEvent<ProtocolService>
    {
        @Required
        override val apiVersion: ApiVersion = API_VERSION
    }


    /**
     * Add the specified study [protocol].
     *
     * @param versionTag An optional label used to identify this first version of the [protocol]. "Initial" by default.
     * @throws IllegalArgumentException when:
     *   - a [protocol] with the same id already exists
     *   - a different [protocol] with the same owner and name in the latest version already exists
     *   - [protocol] is invalid
     */
    suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String = "Initial" )

    /**
     * Add a new version for the specified study [protocol],
     * of which a previous version (a protocol with the same id) is already stored.
     *
     * @param versionTag
     *   An optional unique label used to identify this specific version of the [protocol].
     *   The current date/time by default.
     * @throws IllegalArgumentException when:
     *   - [protocol] is not yet stored in the repository
     *   - a different [protocol] with the same owner and name in the latest version already exists
     *   - [protocol] is invalid
     *   - the [versionTag] is already in use
     */
    suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String = Clock.System.now().toString() )

    /**
     * Replace the expected participant data for the study protocol with the specified [protocolId]
     * and [versionTag] with [expectedParticipantData].
     *
     * @throws IllegalArgumentException when:
     *   - no protocol with [protocolId] is found
     *   - [expectedParticipantData] contains differing [ParticipantAttribute]s with the same input data type
     * @return The updated [StudyProtocolSnapshot].
     */
    suspend fun updateParticipantDataConfiguration(
        protocolId: UUID,
        versionTag: String,
        expectedParticipantData: Set<ExpectedParticipantData>
    ): StudyProtocolSnapshot

    /**
     * Return the [StudyProtocolSnapshot] with the specified [protocolId],
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when a protocol with [protocolId] or [versionTag] does not exist.
     */
    suspend fun getBy( protocolId: UUID, versionTag: String? = null ): StudyProtocolSnapshot

    /**
     * Find all [StudyProtocolSnapshot]'s owned by the owner with [ownerId].
     *
     * @return This returns the last version of each [StudyProtocolSnapshot] owned by the requested owner,
     *   or an empty list when none are found.
     */
    suspend fun getAllForOwner( ownerId: UUID ): List<StudyProtocolSnapshot>

    /**
     * Returns all stored versions for the protocol with the specified [protocolId].
     *
     * @throws IllegalArgumentException when a protocol with [protocolId] does not exist.
     */
    suspend fun getVersionHistoryFor( protocolId: UUID ): List<ProtocolVersion>
}
