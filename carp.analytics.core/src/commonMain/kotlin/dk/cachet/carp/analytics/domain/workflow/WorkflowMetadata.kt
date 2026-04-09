package dk.cachet.carp.analytics.domain.workflow

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val major: Int,
    val minor: Int? = null
)
{
    override fun toString(): String = minor?.let { "$major.$it" } ?: "$major"
}

interface ComponentMetadata
{
    val name: String
    val id: UUID
    val description: String?
    val version: Version
}

@Serializable
data class StepMetadata(
    override val name: String,
    override val id: UUID,
    override val description: String? = null,
    override val version: Version = Version(1),
    val descriptorId: String? = null
) : ComponentMetadata

@Serializable
data class SubWorkflowMetadata(
    override val name: String,
    override val id: UUID,
    override val description: String? = null,
    override val version: Version = Version(1)
) : ComponentMetadata

@Serializable
data class WorkflowMetadata(
    override val name: String,
    override val description: String? = null,
    override val id: UUID,
    override val version: Version = Version(1),
) : ComponentMetadata
