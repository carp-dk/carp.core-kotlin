package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.process.ExternalProcess
import kotlin.reflect.KClass

interface IExecutionFactory {
    /**
     * Registers an Executor for a specific Process type.
     * @param processType The class of the process.
     * @param executorCreator A lambda that creates an Executor instance.
     */
    fun <P : ExternalProcess> register(
        processType: KClass<out P>,
        executorCreator: () -> Executor<P>
    )

    /**
     * Retrieves an Executor for the given Process.
     * @param process The process instance.
     * @return The corresponding [Executor] instance.
     * @throws IllegalArgumentException If no Executor is registered for the given Process type.
     */
    fun <P : ExternalProcess> getExecutor(process: P): Executor<P>
}
