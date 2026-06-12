package dk.cachet.carp.protocols.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.application.users.ParticipantAttribute
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.protocols.domain.StudyProtocolRepository
import kotlin.time.Clock


/**
 * Implementation of [ProtocolService] which allows managing (multiple versions of) [StudyProtocolSnapshot]'s,
 * which can be instantiated locally through [StudyProtocol].
 */
class ProtocolServiceHost(
    private val repository: StudyProtocolRepository,
    private val clock: Clock = Clock.System
) : ProtocolService
{
    /**
     * Add the specified study [protocol].
     *
     * @param versionTag An optional label used to identify this first version of the [protocol]. "Initial" by default.
     * @throws IllegalArgumentException when:
     *   - a [protocol] with the same id already exists
     *   - a different [protocol] with the same owner and name in the latest version already exists
     *   - [protocol] is invalid
     */
    override suspend fun add( protocol: StudyProtocolSnapshot, versionTag: String )
    {
        val initializedProtocol = StudyProtocol.fromSnapshot( protocol )
        repository.add( initializedProtocol, ProtocolVersion( versionTag, clock.now() ) )
    }

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
    override suspend fun addVersion( protocol: StudyProtocolSnapshot, versionTag: String )
    {
        val initializedProtocol = StudyProtocol.fromSnapshot( protocol )
        repository.addVersion( initializedProtocol, ProtocolVersion( versionTag, clock.now() ) )
    }

    /**
     * Replace the expected participant data for the study protocol with the specified [protocolId]
     * and [versionTag] with [expectedParticipantData].
     *
     * @throws IllegalArgumentException when:
     *   - no protocol with [protocolId] is found
     *   - [expectedParticipantData] contains differing [ParticipantAttribute]s with the same input data type
     * @return The updated [StudyProtocolSnapshot].
     */
    override suspend fun updateParticipantDataConfiguration(
        protocolId: UUID,
        versionTag: String,
        expectedParticipantData: Set<ExpectedParticipantData>
    ): StudyProtocolSnapshot
    {
        val protocol = repository.getByOrThrow( protocolId, versionTag )
        val isReplaced = protocol.replaceExpectedParticipantData( expectedParticipantData )

        if ( isReplaced )
        {
            val version = repository
                .getVersionHistoryFor( protocol.id )
                .first { it.tag == versionTag }
            repository.replace( protocol, version )
        }

        return protocol.getSnapshot()
    }

    /**
     * Return the [StudyProtocolSnapshot] with the specified [protocolId],
     *
     * @param versionTag The tag of the specific version of the protocol to return. The latest version is returned when not specified.
     * @throws IllegalArgumentException when a protocol with [protocolId] or [versionTag] does not exist.
     */
    override suspend fun getBy( protocolId: UUID, versionTag: String? ): StudyProtocolSnapshot
    {
        val protocol: StudyProtocol? = repository.getBy( protocolId, versionTag )
        requireNotNull( protocol ) { "No protocol found for the specified owner with the given name and version." }

        return protocol.getSnapshot()
    }

    /**
     * Find all [StudyProtocolSnapshot]'s owned by the owner with [ownerId].
     *
     * @return This returns the last version of each [StudyProtocolSnapshot] owned by the requested owner,
     *   or an empty list when none are found.
     */
    override suspend fun getAllForOwner( ownerId: UUID ): List<StudyProtocolSnapshot> =
        repository.getAllForOwner( ownerId ).map { it.getSnapshot() }.toList()

    /**
     * Returns all stored versions for the protocol with the specified [protocolId].
     *
     * @throws IllegalArgumentException when a protocol with [protocolId] does not exist.
     */
    override suspend fun getVersionHistoryFor( protocolId: UUID ): List<ProtocolVersion> =
        repository.getVersionHistoryFor( protocolId )
}
