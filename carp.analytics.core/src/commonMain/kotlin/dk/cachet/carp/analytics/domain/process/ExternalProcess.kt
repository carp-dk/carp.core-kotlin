package dk.cachet.carp.analytics.domain.process


/**
 * Defines the contract for externally executed processes (e.g., Python scripts, CLI tools).
 */
interface ExternalProcess : WorkflowProcess
{
    fun getArguments(): Any
}
