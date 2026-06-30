package dk.cachet.carp.deployments.domain.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.users.ExpectedParticipantData
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.AssignedPrimaryDevice
import dk.cachet.carp.deployments.application.users.ParticipantData
import kotlinx.serialization.*
import kotlin.time.Instant


/**
 * A serializable snapshot of a [ParticipantGroup] at the moment in time when it was created.
 */
@Serializable
data class ParticipantGroupSnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val studyDeploymentId: UUID,
    val assignedPrimaryDevices: Set<AssignedPrimaryDevice>,
    val isStudyDeploymentStopped: Boolean,
    val expectedData: Set<ExpectedParticipantData> = emptySet(),
    val participations: Set<AccountParticipation> = emptySet(),
    val commonData: Map<InputDataType, Data?> = emptyMap(),
    val roleData: List<ParticipantData.RoleData> = emptyList()
) : Snapshot<ParticipantGroup>
{
    companion object
    {
        /**
         * Create a snapshot of the specified participant [group] using the specified snapshot [version].
         */
        fun fromParticipantGroup( group: ParticipantGroup, version: Int ): ParticipantGroupSnapshot =
            ParticipantGroupSnapshot(
                group.id,
                group.createdOn,
                version,
                group.studyDeploymentId,
                group.assignedPrimaryDevices.toSet(),
                group.isStudyDeploymentStopped,
                group.expectedData.toSet(),
                group.participations.toSet(),
                group.commonData.toMap(),
                group.roleData.toList()
            )
    }


    override fun toObject(): ParticipantGroup = ParticipantGroup.fromSnapshot( this )
}
