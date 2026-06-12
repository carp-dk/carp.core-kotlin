package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.DefaultUUIDFactory
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.UUIDFactory
import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import kotlin.time.Clock


/**
 * Implementation of [StudyService] which allows creating and managing studies.
 */
class StudyServiceHost(
    private val repository: StudyRepository,
    private val eventBus: ApplicationServiceEventBus<StudyService, StudyService.Event>,
    private val uuidFactory: UUIDFactory = DefaultUUIDFactory,
    private val clock: Clock = Clock.System
) : StudyService
{
    /**
     * Create a new study for the entity (e.g., person or group) with [ownerId].
     */
    override suspend fun createStudy(
        ownerId: UUID,
        /**
         * A descriptive name for the study, assigned by, and only visible to, the entity with [ownerId].
         */
        name: String,
        /**
         * An optional description of the study, assigned by, and only visible to, the entity with [ownerId].
         */
        description: String?,
        /**
         * An optional description of the study, shared with participants once they are invited.
         * In case no description is specified, [name] is used as the name in [invitation].
         */
        invitation: StudyInvitation?
    ): StudyStatus
    {
        val ensuredInvitation = invitation ?: StudyInvitation( name )
        val study = Study( ownerId, name, description, ensuredInvitation, uuidFactory.randomUUID(), clock.now() )

        repository.add( study )
        eventBus.publish( StudyService.Event.StudyCreated( study.getStudyDetails() ) )

        return study.getStatus()
    }

    /**
     * Set study details which are visible only to the study owner.
     *
     * @param studyId The id of the study to update the study details for.
     * @param name A descriptive name for the study.
     * @param description A description of the study; null to remove description.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun setInternalDescription( studyId: UUID, name: String, description: String? ): StudyStatus
    {
        val study = repository.getById( studyId )
        requireNotNull( study )

        study.name = name
        study.description = description
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Gets detailed information about the study with the specified [studyId], including which study protocol is set.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun getStudyDetails( studyId: UUID ): StudyDetails
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        return study.getStudyDetails()
    }

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a deployment with [studyId] does not exist.
     */
    override suspend fun getStudyStatus( studyId: UUID ): StudyStatus
    {
        val study = repository.getById( studyId )
        requireNotNull( study )

        return study.getStatus()
    }

    /**
     * Get status for all studies created by the entity (e.g. person or group) with the specified [ownerId].
     */
    override suspend fun getStudiesOverview( ownerId: UUID ): List<StudyStatus> =
        repository.getForOwner( ownerId ).map { it.getStatus() }

    /**
     * Specify an [invitation], shared with participants once they are invited to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    override suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        study.invitation = invitation
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Specify the study [protocol] to use for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - the provided [protocol] snapshot is invalid
     *  - the [protocol] contains errors preventing it from being used in deployments
     * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
     */
    override suspend fun setProtocol( studyId: UUID, protocol: StudyProtocolSnapshot ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        // Configure study to use the protocol.
        study.protocolSnapshot = protocol
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Remove the currently set study protocol for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
     */
    override suspend fun removeProtocol( studyId: UUID ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        study.protocolSnapshot = null
        repository.update( study )

        return study.getStatus()
    }

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when no study protocol for the given study is set yet.
     */
    override suspend fun goLive( studyId: UUID ): StudyStatus
    {
        val study: Study? = repository.getById( studyId )
        requireNotNull( study )

        if ( !study.isLive )
        {
            study.goLive()
            eventBus.publish( StudyService.Event.StudyGoneLive( study.getStudyDetails() ) )
            repository.update( study )
        }

        return study.getStatus()
    }

    /**
     * Remove the study with the specified [studyId] and all related data.
     *
     * @return True when the study has been deleted, or false when there is no study to delete.
     */
    override suspend fun remove( studyId: UUID ): Boolean
    {
        val isRemoved = repository.remove( studyId )

        if ( isRemoved )
        {
            eventBus.publish( StudyService.Event.StudyRemoved( studyId ) )
        }

        return isRemoved
    }
}
