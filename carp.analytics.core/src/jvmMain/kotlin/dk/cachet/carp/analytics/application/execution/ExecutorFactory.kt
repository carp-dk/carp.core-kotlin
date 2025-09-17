package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.domain.process.CommandLineExternalProcess
import dk.cachet.carp.analytics.domain.process.ExternalProcess
import dk.cachet.carp.analytics.domain.process.PythonExternalProcess
import dk.cachet.carp.analytics.infrastructure.execution.CommandLineExecutor
import dk.cachet.carp.analytics.infrastructure.execution.PythonExecutor
import kotlin.reflect.KClass

/**
 * Factory for creating Executor instances dynamically using a registration-based model.
 */
object ExecutorFactory
{

    private val registry: MutableMap<KClass<out ExternalProcess>, () -> Executor<*>> = mutableMapOf()


    /**
     * Registers an Executor for a specific Process type.
     * @param processType The class of the process.
     * @param executorCreator A lambda that creates an Executor instance.
     */
    fun <P : ExternalProcess> register( processType: KClass<out P>, executorCreator: () -> Executor<P> )
    {
        registry[processType] = executorCreator
    }

    /**
     * Retrieves an Executor for the given Process.
     * @param process The process instance.
     * @return The corresponding Executor instance.
     * @throws IllegalArgumentException If no Executor is registered for the given Process type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : ExternalProcess> getExecutor( process: P ): Executor<P>
    {
        val creator = registry[process::class]
            ?: throw IllegalArgumentException("Unsupported process type: ${process::class.simpleName}")
        return creator() as Executor<P>
    }

    /**
     * Registers core executors.
     */
    init
    {
        register(CommandLineExternalProcess::class) { CommandLineExecutor() }
        register(PythonExternalProcess::class) { PythonExecutor() }
    }
}
