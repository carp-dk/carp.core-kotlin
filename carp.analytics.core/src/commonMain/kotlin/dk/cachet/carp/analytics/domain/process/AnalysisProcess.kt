package dk.cachet.carp.analytics.domain.process

import dk.cachet.carp.analytics.domain.data.ICarpTabularData

/**
 * Defines the contract for in-memory analytics processes.
 */
interface AnalysisProcess : WorkflowProcess
{
    fun process( input: ICarpTabularData ): ICarpTabularData?
}
