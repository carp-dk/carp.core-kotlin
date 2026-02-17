package dk.cachet.carp.analytics.domain.environment

import dk.cachet.carp.common.application.UUID

/**
 * Declarative author-time definition of an execution environment.
 *
 * Contains only specification data (dependencies, variables).
 * Does not represent a materialized/runtime environment.
 */
interface EnvironmentDefinition
{
    val id: UUID
    val name: String
    val dependencies: List<String>
    val environmentVariables: Map<String, String>
}

