@file:Suppress( "NON_EXPORTABLE_TYPE" )

package dk.cachet.carp.studies.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A group of one or more [participants] which is first [Staged] to later be [Invited] to a [StudyDeployment].
 * Once [Invited], the participant group is [InDeployment] and will get the state [Running] once the deployment is running,
 * until the deployment is [Stopped].
 */
@Serializable
@JsExport
sealed class ParticipantGroupStatus
{
    /**
     * The ID of this participant group, which is equivalent to the ID of the associated study deployment once deployed.
     */
    abstract val id: UUID

    /**
     * Optional metadata representing the group of participants.
     */
    abstract val representation: ParticipantGroupRepresentation

    /**
     * The participants that are part of this group.
     */
    abstract val participants: Set<Participant>

    /**
     * The participant role assignments in this group.
     */
    abstract val assignedParticipantRoles: Set<AssignedParticipantRoles>


    /**
     * The [participants] have not yet been invited. The list of participants can still be modified.
     */
    @Serializable
    data class Staged(
        override val id: UUID,
        override val participants: Set<Participant>,
        override val assignedParticipantRoles: Set<AssignedParticipantRoles>,
        @Required
        override val representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
    ) : ParticipantGroupStatus()


    /**
     * A [ParticipantGroup] that has been invited to a [StudyDeployment].
     */
    @Serializable
    sealed class InDeployment : ParticipantGroupStatus()
    {
        companion object
        {
            /**
             * Initialize an [InDeployment] state for a group of [participants] based on [deploymentStatus].
             */
            fun fromDeploymentStatus(
                participants: Set<Participant>,
                roleAssignment: Set<AssignedParticipantRoles>,
                deploymentStatus: StudyDeploymentStatus,
                representation: ParticipantGroupRepresentation
            ): InDeployment
            {
                val id = deploymentStatus.studyDeploymentId
                val createdOn: Instant = deploymentStatus.createdOn
                val startedOn: Instant? = deploymentStatus.startedOn

                return when ( deploymentStatus )
                {
                    is StudyDeploymentStatus.Invited,
                    is StudyDeploymentStatus.DeployingDevices ->
                        // If deployment was ready at one point (`startedOn`), consider the study 'Running'.
                        if ( startedOn == null )
                        {
                            Invited( id, participants, roleAssignment, createdOn, deploymentStatus, representation )
                        }
                        else
                        {
                            Running(
                                id,
                                participants,
                                roleAssignment,
                                createdOn,
                                deploymentStatus,
                                startedOn,
                                representation
                            )
                        }
                    is StudyDeploymentStatus.Running ->
                        Running(
                            id,
                            participants,
                            roleAssignment,
                            createdOn,
                            deploymentStatus,
                            checkNotNull( startedOn ),
                            representation
                        )
                    is StudyDeploymentStatus.Stopped ->
                        Stopped(
                            id,
                            participants,
                            roleAssignment,
                            createdOn,
                            deploymentStatus,
                            startedOn,
                            deploymentStatus.stoppedOn,
                            representation
                        )
                }
            }
        }


        /**
         * The time at which the participant group was invited.
         */
        abstract val invitedOn: Instant
        /**
         * The deployment status of the study deployment the participants were invited to.
         */
        abstract val studyDeploymentStatus: StudyDeploymentStatus
    }


    /**
     * The [participants] have been invited to a study deployment which isn't [Running] or hasn't been [Stopped] yet.
     * More details are on the study deployment state are available in [studyDeploymentStatus].
     */
    @Serializable
    data class Invited(
        override val id: UUID,
        override val participants: Set<Participant>,
        override val assignedParticipantRoles: Set<AssignedParticipantRoles>,
        override val invitedOn: Instant,
        override val studyDeploymentStatus: StudyDeploymentStatus,
        @Required
        override val representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
    ) : InDeployment()

    /**
     * The study deployment is [StudyDeploymentStatus.Running],
     * of which more details are available in [studyDeploymentStatus].
     */
    @Serializable
    data class Running(
        override val id: UUID,
        override val participants: Set<Participant>,
        override val assignedParticipantRoles: Set<AssignedParticipantRoles>,
        override val invitedOn: Instant,
        override val studyDeploymentStatus: StudyDeploymentStatus,
        /**
         * The time when the study deployment started running, i.e., when all devices were deployed for the first time.
         */
        val startedOn: Instant,
        @Required
        override val representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
    ) : InDeployment()

    /**
     * The study deployment has [StudyDeploymentStatus.Stopped],
     * of which more details are available in [studyDeploymentStatus].
     */
    @Serializable
    data class Stopped(
        override val id: UUID,
        override val participants: Set<Participant>,
        override val assignedParticipantRoles: Set<AssignedParticipantRoles>,
        override val invitedOn: Instant,
        override val studyDeploymentStatus: StudyDeploymentStatus,
        /**
         * The time when the study deployment was ready for the first time (all devices deployed),
         * or null in case this was never the case.
         */
        val startedOn: Instant?,
        /**
         * The time when the study deployment was stopped.
         */
        val stoppedOn: Instant,
        @Required
        override val representation: ParticipantGroupRepresentation = ParticipantGroupRepresentation.Default
    ) : InDeployment()
}
