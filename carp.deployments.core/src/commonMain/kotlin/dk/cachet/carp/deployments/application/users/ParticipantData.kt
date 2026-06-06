package dk.cachet.carp.deployments.application.users

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Set data for all expected participant data in the study deployment with [studyDeploymentId].
 * Data which is not set equals null.
 */
@Serializable
@JsExport
data class ParticipantData(
    val studyDeploymentId: UUID,
    /**
     * Data that is related to everyone in the study deployment.
     */
    val common: Map<InputDataType, Data?>,
    /**
     * Data that is related to specific roles in the study deployment.
     */
    val roles: List<RoleData>
)
{
    @Serializable
    data class RoleData( val roleName: String, val data: Map<InputDataType, Data?> )
}
