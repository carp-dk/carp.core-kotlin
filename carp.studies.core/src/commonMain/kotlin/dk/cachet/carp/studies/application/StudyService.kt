package dk.cachet.carp.studies.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.protocols.application.VersionedStudyProtocolSnapshot
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


/**
 * Application service which allows creating and managing studies.
 */
interface StudyService : ApplicationService<StudyService, StudyService.Event>
{
    companion object { val API_VERSION = ApiVersion( 1, 3 ) }

    @Serializable
    sealed class Event( override val aggregateId: String? ) : IntegrationEvent<StudyService>
    {
        constructor( aggregateId: UUID ) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

        @Serializable
        data class StudyCreated( val study: StudyDetails ) : Event( study.studyId )

        @Serializable
        data class StudyGoneLive( val study: StudyDetails ) : Event( study.studyId )

        @Serializable
        data class StudyRemoved( val studyId: UUID ) : Event( studyId )
    }


    /**
     * Create a new study for the entity (e.g., person or group) with [ownerId].
     */
    suspend fun createStudy(
        ownerId: UUID,
        /**
         * A descriptive name for the study, assigned by, and only visible to, the entity with [ownerId].
         */
        name: String,
        /**
         * An optional description of the study, assigned by, and only visible to, the entity with [ownerId].
         */
        description: String? = null,
        /**
         * An optional description of the study, shared with participants once they are invited.
         * In case no description is specified, [name] is used as the name in [invitation].
         */
        invitation: StudyInvitation? = null
    ): StudyStatus

    /**
     * Set study details which are visible only to the study owner.
     *
     * @param studyId The id of the study to update the study details for.
     * @param name A descriptive name for the study.
     * @param description A description of the study; null to remove description.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun setInternalDescription( studyId: UUID, name: String, description: String? ): StudyStatus

    /**
     * Gets detailed information about the study with the specified [studyId], including which study protocol is set.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getStudyDetails( studyId: UUID ): StudyDetails

    /**
     * Get the status for a study with the given [studyId].
     *
     * @param studyId The id of the study to return [StudyStatus] for.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun getStudyStatus( studyId: UUID ): StudyStatus

    /**
     * Get status for all studies created by the entity (e.g. person or group) with the specified [ownerId].
     */
    suspend fun getStudiesOverview( ownerId: UUID ): List<StudyStatus>

    /**
     * Specify an [invitation], shared with participants once they are invited to the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     */
    suspend fun setInvitation( studyId: UUID, invitation: StudyInvitation ): StudyStatus

    /**
     * Specify the study [protocol] to use for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when:
     *  - a study with [studyId] does not exist
     *  - the provided [protocol] snapshot is invalid
     *  - the [protocol] contains errors preventing it from being used in deployments
     * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
     */
    suspend fun setProtocol( studyId: UUID, protocol: VersionedStudyProtocolSnapshot ): StudyStatus

    /**
     * Remove the currently set study protocol for the study with the specified [studyId].
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when the study protocol can no longer be set since the study went 'live'.
     */
    suspend fun removeProtocol( studyId: UUID ): StudyStatus

    /**
     * Lock in the current study protocol so that the study may be deployed to participants.
     *
     * @throws IllegalArgumentException when a study with [studyId] does not exist.
     * @throws IllegalStateException when no study protocol for the given study is set yet.
     */
    suspend fun goLive( studyId: UUID ): StudyStatus

    /**
     * Remove the study with the specified [studyId] and all related data.
     *
     * @return True when the study has been deleted, or false when there is no study to delete.
     */
    suspend fun remove( studyId: UUID ): Boolean
}
