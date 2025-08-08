package dk.cachet.carp.deployments.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.users.ParticipantStatus
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import kotlinx.datetime.Instant
import kotlinx.serialization.*


/**
 * A serializable snapshot of a [StudyDeployment] at the moment in time when it was created.
 */
@Serializable
data class StudyDeploymentSnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val studyProtocolSnapshot: StudyProtocolSnapshot,
    val participants: List<ParticipantStatus>,
    val registeredDevices: Set<String> = emptySet(),
    val deviceRegistrationHistory: Map<String, List<DeviceRegistration>> = emptyMap(),
    val deployedDevices: Set<String> = emptySet(),
    val invalidatedDeployedDevices: Set<String> = emptySet(),
    val startedOn: Instant?,
    val stoppedOn: Instant?
) : Snapshot<StudyDeployment>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [studyDeployment] using the specified snapshot [version].
         */
        fun fromDeployment( studyDeployment: StudyDeployment, version: Int ): StudyDeploymentSnapshot
        {
            return StudyDeploymentSnapshot(
                studyDeployment.id,
                studyDeployment.createdOn,
                version,
                studyDeployment.protocolSnapshot,
                studyDeployment.participants.toList(),
                studyDeployment.registeredDevices.map { it.key.roleName }.toSet(),
                studyDeployment.deviceRegistrationHistory.mapKeys { it.key.roleName },
                studyDeployment.deployedDevices.map { it.roleName }.toSet(),
                studyDeployment.invalidatedDeployedDevices.map { it.roleName }.toSet(),
                studyDeployment.startedOn,
                studyDeployment.stoppedOn
            )
        }
    }


    override fun toObject(): StudyDeployment = StudyDeployment.fromSnapshot( this )
}
