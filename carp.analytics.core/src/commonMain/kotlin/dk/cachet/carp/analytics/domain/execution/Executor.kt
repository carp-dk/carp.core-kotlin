package dk.cachet.carp.analytics.domain.execution

import dk.cachet.carp.analytics.domain.workflow.Step

/**
 * Executor interface for executing workflow steps.
 *
 * Executors are responsible for the execution lifecycle of a step, including:
 * - Setting up the execution environment
 * - Executing the step's process with its inputs/outputs
 * - Cleaning up resources after execution
 *
 * The executor receives a complete [Step] which contains:
 * - The process to execute
 * - The execution context (environment, variables)
 * - Input/output specifications
 */
interface Executor
{
    /**
     * Sets up the execution environment for the step.
     * This may include creating/validating environments, preparing resources, etc.
     *
     * @param step The step to set up.
     */
    fun setup( step: Step ) {}

    /**
     * Executes the step's process with its configured inputs and outputs.
     *
     * @param step The step to execute.
     */
    fun execute( step: Step )

}
