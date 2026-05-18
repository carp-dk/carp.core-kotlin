package dk.cachet.carp.deployments.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.CarpInputDataTypes
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.devices.isPrimary
import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.domain.users.ParticipationRepository
import dk.cachet.carp.deployments.domain.users.filterActiveParticipationInvitations


/**
 * Application service which allows retrieving participations for study deployments,
 * and managing data related to participants which is input by users.
 */
class ParticipationServiceHost(
    private val participationRepository: ParticipationRepository,
    private val participantGroupService: ParticipantGroupService,
    private val eventBus: ApplicationServiceEventBus<ParticipationService, ParticipationService.Event>,
    /**
     * Supported [InputDataType]'s for participant data input by users.
     */
    private val participantDataInputTypes: InputDataTypeList = CarpInputDataTypes
) : ParticipationService
{
    init
    {
        eventBus.subscribe {
            // Create a ParticipantGroup per study deployment (as long as it exists).
            event { created: DeploymentService.Event.StudyDeploymentCreated ->
                val group = participantGroupService.createAndInviteParticipantGroup( created )
                participationRepository.putParticipantGroup( group )
            }
            event { removed: DeploymentService.Event.StudyDeploymentRemoved ->
                participationRepository.removeParticipantGroups( setOf( removed.studyDeploymentId ) )
            }

            // Notify participant group that associated study deployment has stopped.
            event { stopped: DeploymentService.Event.StudyDeploymentStopped ->
                val group = participationRepository.getParticipantGroup( stopped.studyDeploymentId )
                checkNotNull( group )
                group.studyDeploymentStopped()
                participationRepository.putParticipantGroup( group )
            }

            // Keep track of primary device registration changes.
            event { registrationChange: DeploymentService.Event.DeviceRegistrationChanged ->
                if ( !registrationChange.device.isPrimary() ) return@event

                val group = participationRepository.getParticipantGroup( registrationChange.studyDeploymentId )
                checkNotNull( group )
                group.updateDeviceRegistration( registrationChange.device, registrationChange.registration )
                participationRepository.putParticipantGroup( group )
            }
        }
    }


    /**
     * Get all participations of active study deployments the account with the given [accountId] has been invited to.
     */
    override suspend fun getActiveParticipationInvitations( accountId: UUID ): Set<ActiveParticipationInvitation>
    {
        // Get participant group for each of the account's invitations.
        val invitations = participationRepository.getParticipationInvitations( accountId )
        val deploymentIds = invitations.map { it.participation.studyDeploymentId }.toSet()
        val groups = participationRepository.getParticipantGroupList( deploymentIds )

        return filterActiveParticipationInvitations( invitations, groups )
    }

    /**
     * Get currently set data for all expected participant data in the study deployment with [studyDeploymentId].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when there is no study deployment with [studyDeploymentId].
     */
    override suspend fun getParticipantData( studyDeploymentId: UUID ): ParticipantData
    {
        val group = participationRepository.getParticipantGroupOrThrowBy( studyDeploymentId )

        return ParticipantData( group.studyDeploymentId, group.commonData.toMap(), group.roleData.toList() )
    }

    /**
     * Get currently set data for all expected participant data for a set of study deployments with [studyDeploymentIds].
     * Data which is not set equals null.
     *
     * @throws IllegalArgumentException when [studyDeploymentIds] contains an ID for which no deployment exists.
     */
    override suspend fun getParticipantDataList( studyDeploymentIds: Set<UUID> ): List<ParticipantData>
    {
        val groups = participationRepository.getParticipantGroupListOrThrow( studyDeploymentIds )

        return groups.map { ParticipantData( it.studyDeploymentId, it.commonData.toMap(), it.roleData.toList() ) }
    }

    /**
     * Set [data] that was [inputByParticipantRole] in the study deployment with [studyDeploymentId],
     * or unset it by passing `null`.
     * When you want to set data that was assigned to a specific participant role,
     * [inputByParticipantRole] needs to be set.
     * You can still set common data (assigned to all roles) in the same call.
     *
     * @throws IllegalArgumentException when:
     *   - there is no study deployment with [studyDeploymentId]
     *   - one or more of the keys in [data] isn't configured as expected participant data in the study protocol
     *   - one or more of the set [data] isn't valid for the corresponding input data type
     * @return All data for the specified study deployment, including the newly set data.
     */
    override suspend fun setParticipantData(
        studyDeploymentId: UUID,
        data: Map<InputDataType, Data?>,
        /**
         * The participant role who filled out [data]; null if all roles can set it.
         */
        inputByParticipantRole: String?
    ): ParticipantData
    {
        val group = participationRepository.getParticipantGroupOrThrowBy( studyDeploymentId )
        group.setData( participantDataInputTypes, data, inputByParticipantRole )
        participationRepository.putParticipantGroup( group )

        return ParticipantData( group.studyDeploymentId, group.commonData.toMap(), group.roleData.toList() )
    }
}
