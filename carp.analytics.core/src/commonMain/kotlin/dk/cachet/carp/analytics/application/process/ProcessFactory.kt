package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.process.WorkflowProcess

/**
 * Factory contract to create a [WorkflowProcess] from a parsed YAML section.
 * The provided spec is the map for a single process section (type, name, config, ...).
 */
interface ProcessFactory {
    /** The key in the YAML section that identifies the process type (e.g., "type"). */
    val typeKey: String

    /**
     * Registered builders keyed by stable typeId (e.g., "python_script").
     * Implementations may keep this immutable.
     */
    val registry: Map<String, (spec: Map<String, Any?>) -> WorkflowProcess>

    /** Create a process instance based on the supplied YAML section. */
    fun create(spec: Map<String, Any?>): WorkflowProcess
}

/**
 * A minimal, multiplatform default implementation.
 * - [registry] maps a stable typeId (e.g., "python_script") to a builder function.
 * - [typeKey] is the key in the YAML section that identifies the process type (defaults to "type").
 *
 * Note: This class holds an immutable registry; extend by composing a new instance with a larger map.
 */
class DefaultProcessFactory(
    override val registry: Map<String, (spec: Map<String, Any?>) -> WorkflowProcess>,
    override val typeKey: String = "type"
) : ProcessFactory {
    override fun create(spec: Map<String, Any?>): WorkflowProcess {
        val typeId = spec[typeKey] as? String
            ?: error("Missing '$typeKey' in process specification.")
        val builder = registry[typeId]
            ?: error("No process builder registered for typeId '$typeId'. Registered: ${registry.keys}")
        return builder(spec)
    }
}