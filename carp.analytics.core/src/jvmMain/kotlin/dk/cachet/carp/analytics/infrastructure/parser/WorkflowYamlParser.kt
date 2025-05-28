package dk.cachet.carp.analytics.infrastructure.parser

import dk.cachet.carp.analytics.domain.workflow.Workflow

/**
 * Object responsible for parsing [Workflow] definitions from YAML strings.
 *
 * This parser only performs deserialization — no runtime injection is performed here.
 * Runtime dependencies (e.g., services, registries) must be injected separately using
 * [dk.cachet.carp.analytics.application.execution.WorkflowInjector].
 */
object WorkflowYamlParser {

    /**
     * Deserialize a [Workflow] object from a raw YAML [content] string.
     *
     * @param content The YAML content representing a workflow.
     * @return A deserialized [Workflow] object.
     */
    fun fromString(content: String): Workflow {
        return WorkflowYaml.decodeFromString(Workflow.serializer(), content)
    }
}
