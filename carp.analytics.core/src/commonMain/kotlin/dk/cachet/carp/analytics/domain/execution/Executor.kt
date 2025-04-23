package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.process.Process

/**
 * Executor interface for executing specific process types.
 * @param P The specific type of Process that this executor supports.
 */
interface Executor<P : Process> {
    fun setup(process: P, context: ExecutionContext){}
    fun execute(process: P, context: ExecutionContext) 
    fun cleanup(process: P, context: ExecutionContext){}
}