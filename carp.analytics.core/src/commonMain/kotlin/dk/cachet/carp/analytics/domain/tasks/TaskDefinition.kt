package dk.cachet.carp.analytics.domain.tasks

import dk.cachet.carp.common.application.UUID

/**
 * Declarative author-time definition of a unit of work executed as part of a workflow step.
 *
 * This is a definition only (identity + metadata/config). Runtime execution behaviour belongs to
 * Plan/Execute components, not the domain model.
 */
interface TaskDefinition
{
    val id: UUID
    val name: String
    val description: String?
}
