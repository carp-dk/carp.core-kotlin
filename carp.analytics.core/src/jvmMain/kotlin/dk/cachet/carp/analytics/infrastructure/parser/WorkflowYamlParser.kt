package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * YAML parser for loading [Workflow] definitions from string or file sources.
 */
object WorkflowYamlParser {

    /**
     * Parse a [Workflow] from a raw YAML string.
     */
    fun fromString(content: String): Workflow =
        WorkflowYaml.decodeFromString(Workflow.serializer(), content)

}
