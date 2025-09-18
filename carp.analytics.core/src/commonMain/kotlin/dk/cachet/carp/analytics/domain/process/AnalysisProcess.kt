package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.data.application.ImmutableDataStreamBatch

/**
 * Defines the contract for in-memory analytics processes.
 */
interface AnalysisProcess : WorkflowProcess {
    fun process(input: ImmutableDataStreamBatch): ImmutableDataStreamBatch?
}
