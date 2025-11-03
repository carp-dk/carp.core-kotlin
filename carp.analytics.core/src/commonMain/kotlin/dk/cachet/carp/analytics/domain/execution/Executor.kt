package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.process.ExternalProcess

/**
 * Executor interface for executing specific process types.
 * @param P The specific type of Process that this executor supports.
 */
interface Executor<P : ExternalProcess>
{
    fun setup( process: P, context: ExecutionContext ){}
    fun execute( process: P, context: ExecutionContext )
    fun cleanup( process: P, context: ExecutionContext ){}
}
