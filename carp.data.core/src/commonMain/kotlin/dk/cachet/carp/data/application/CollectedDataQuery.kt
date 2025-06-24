package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CollectedDataQuery(
    val studyDeploymentIds: Set<UUID>? = null,
    val deviceRoleNames: Set<String>? = null,
    val fields: Set<String>? = null,
    val from: Instant? = null,
    val to: Instant? = null,
    val offsetDays: Int? = null
)
