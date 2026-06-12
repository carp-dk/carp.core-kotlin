package dk.cachet.carp.clients.domain.study

import dk.cachet.carp.clients.application.study.StudyStatus
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.domain.Snapshot
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import kotlinx.serialization.*
import kotlin.time.Instant


@Serializable
data class StudySnapshot(
    override val id: UUID,
    override val createdOn: Instant,
    override val version: Int,
    val studyDeploymentId: UUID,
    val deviceRoleName: String,
    val deploymentStatus: StudyDeploymentStatus?,
    val deploymentInformation: PrimaryDeviceDeployment?
) : Snapshot<Study>
{
    companion object
    {
        /**
         * Create a snapshot of the specified [study] using the specified snapshot [version].
         */
        fun fromStudy( study: Study, version: Int ): StudySnapshot
        {
            val status = study.getStatus()
            val deploymentInformation: PrimaryDeviceDeployment? =
                when ( status )
                {
                    is StudyStatus.DeviceDeploymentReceived -> status.deploymentInformation
                    is StudyStatus.Stopped -> status.deploymentInformation
                    else -> null
                }

            return StudySnapshot(
                study.id,
                study.createdOn,
                version,
                study.studyDeploymentId,
                study.deviceRoleName,
                (status as? StudyStatus.DeploymentStatusAvailable)?.deploymentStatus,
                deploymentInformation
            )
        }
    }

    override fun toObject(): Study = Study.fromSnapshot( this )
}
