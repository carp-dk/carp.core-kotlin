package dk.cachet.carp.analytics.application.execution

import dk.cachet.carp.analytics.domain.process.Process
import dk.cachet.carp.analytics.domain.execution.Executor
import dk.cachet.carp.analytics.infrastructure.execution.CommandLineExecutor
import dk.cachet.carp.analytics.infrastructure.execution.PythonExecutor

import dk.cachet.carp.analytics.domain.process.CommandLineProcess
import dk.cachet.carp.analytics.domain.process.PythonProcess

import kotlin.reflect.KClass

/**
 * Factory for creating Executor instances dynamically using a registration-based model.
 */
object ExecutorFactory {

    private val registry: MutableMap<KClass<out Process>, () -> Executor<*>> = mutableMapOf()

    /**
     * Registers an Executor for a specific Process type.
     * @param processType The class of the process.
     * @param executorCreator A lambda that creates an Executor instance.
     */
    fun <P : Process> register(processType: KClass<out P>, executorCreator: () -> Executor<P>) {
        registry[processType] = executorCreator
    }

    /**
     * Retrieves an Executor for the given Process.
     * @param process The process instance.
     * @return The corresponding Executor instance.
     * @throws IllegalArgumentException If no Executor is registered for the given Process type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Process> getExecutor(process: P): Executor<P> {
        val creator = registry[process::class]
            ?: throw IllegalArgumentException("Unsupported process type: ${process::class.simpleName}")
        return creator() as Executor<P>
    }

    /**
     * Registers core executors.
     */
    init {
        register(CommandLineProcess::class) { CommandLineExecutor() }
        register(PythonProcess::class) { PythonExecutor() }
    }
}