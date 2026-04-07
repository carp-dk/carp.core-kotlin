package dk.cachet.carp.analytics.domain.validation

/**
 * Author-time validation for a "Workflow-like" object.
 *
 * This validator provides flexible workflow-level validation by accepting extractors (lambdas)
 * rather than binding to a concrete Workflow model shape. It can validate any workflow-like
 * structure as long as extractors are provided for the necessary data.
 *
 * Validation checks:
 * - **Unique step IDs**: Ensures no two steps have the same id within a workflow
 * - **Valid dependencies**: Ensures all referenced dependencies point to existing steps
 * - **No cycles**: Detects circular dependencies in the workflow
 *
 * Usage:
 * ```
 * val config = WorkflowValidator.Config(
 *     workflowName = { workflow -> workflow.id },
 *     steps = { workflow -> workflow.steps },
 *     stepMetadata = { step -> step.id },
 *     dependencies = { step -> step.dependsOn },
 *     hasDependencyGraph = true
 * )
 * val result = WorkflowValidator.validate(workflow, config)
 * ```
 */
object WorkflowValidator
{

    /**
     * Configuration for validating a workflow-like object.
     *
     * @param W The workflow type to validate
     * @param S The step type within the workflow
     * @param workflowId Extract the unique id from a workflow
     * @param steps Extract the list of steps from a workflow
     * @param stepId Extract the unique id from a step
     * @param dependencies Extract dependency step IDs from a step (defaults to empty list if no dependencies)
     * @param hasDependencyGraph Whether to perform dependency graph validation (refs and cycles). Defaults to true
     * @param workflowPath Generate a path string for error reporting (defaults to "workflows[{workflowName}]")
     * @param stepPath Generate a path string for a specific step in error reporting (defaults to "steps[{stepMetadata}]")
     */
    data class Config<W, S>(
        val workflowId: (W) -> String,
        val steps: (W) -> List<S>,
        val stepId: (S) -> String,
        val dependencies: (S) -> List<String> = { emptyList() },
        val hasDependencyGraph: Boolean = true,
        val workflowPath: (W) -> String = { w -> "workflows[${workflowId( w )}]" },
        val stepPath: (W, S) -> String = { _, s -> "steps[${stepId( s )}]" }
    )

    /**
     * Validates a workflow against the provided configuration.
     *
     * Performs the following checks:
     * - Uniqueness of step IDs within the workflow
     * - Validity of all step dependency references
     * - Absence of circular dependencies (if [Config.hasDependencyGraph] is true)
     *
     * @param workflow The workflow instance to validate
     * @param config The validation configuration with extractors
     * @return A [ValidationResult] containing any validation issues found, or [ValidationResult.OK] if all checks pass
     */
    fun <W, S> validate( workflow: W, config: Config<W, S> ): ValidationResult
    {
        val issues = buildList {
            addAll( checkUniqueStepIds( workflow, config ) )
            if ( config.hasDependencyGraph )
            {
                addAll( checkDependencyReferences( workflow, config ) )
                addAll( checkNoCycles( workflow, config ) )
            }
        }
        return if ( issues.isEmpty() ) ValidationResult.OK else ValidationResult( issues )
    }

    /**
     * Checks that all step IDs within a workflow are unique.
     *
     * @param workflow The workflow to validate
     * @param config The validation configuration
     * @return A list of validation issues for each duplicate step ID found
     */
    private fun <W, S> checkUniqueStepIds( workflow: W, config: Config<W, S> ): List<ValidationIssue>
    {
        val sidList = config.steps( workflow ).map { config.stepId( it ) }
        val duplicates = sidList.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if ( duplicates.isEmpty() ) return emptyList()

        val wid = config.workflowId( workflow )
        val wPath = config.workflowPath( workflow )

        return duplicates.map { dup ->
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.WORKFLOW_STEP_ID_DUPLICATE,
                message = "Workflow '$wid' has duplicate step id '$dup'. Step ids must be unique within a workflow.",
                path = "$wPath.steps",
                subjectId = wid
            )
        }
    }

    /**
     * Checks that all step dependencies reference existing steps.
     *
     * This ensures that no step declares a dependency on a step that doesn't exist in the workflow.
     *
     * @param workflow The workflow to validate
     * @param config The validation configuration
     * @return A list of validation issues for each invalid dependency reference found
     */
    private fun <W, S> checkDependencyReferences( workflow: W, config: Config<W, S> ): List<ValidationIssue>
    {
        val steps = config.steps( workflow )
        val stepIds = steps.map { config.stepId( it ) }.toSet()
        val wid = config.workflowId( workflow )

        val issues = mutableListOf<ValidationIssue>()

        for ( s in steps )
        {
            val sid = config.stepId( s )
            for ( dep in config.dependencies( s ) )
            {
                if ( dep !in stepIds )
                {
                    issues += ValidationIssue(
                        severity = ValidationSeverity.ERROR,
                        code = ValidationErrorCode.WORKFLOW_DEP_REFERENCE_MISSING,
                        message = "Workflow '$wid' step '$sid' references missing dependency '$dep'.",
                        path = "${config.stepPath( workflow, s )}.dependencies",
                        subjectId = wid
                    )
                }
            }
        }

        return issues
    }

    /**
     * Checks for circular dependencies in the workflow.
     *
     * Uses depth-first search to detect cycles in the dependency graph. If cycles are found,
     * returns one or more cycle paths to help developers identify the problematic dependencies.
     *
     * @param workflow The workflow to validate
     * @param config The validation configuration
     * @return A list of validation issues for each cycle detected
     */
    private fun <W, S> checkNoCycles( workflow: W, config: Config<W, S> ): List<ValidationIssue>
    {
        val steps = config.steps( workflow )
        val adj = steps.associate { s ->
            val sid = config.stepId( s )
            sid to config.dependencies( s )
        }

        val cycles = detectCycles( adj )
        if ( cycles.isEmpty() ) return emptyList()

        val wid = config.workflowId( workflow )
        val wPath = config.workflowPath( workflow )

        return cycles.map { cycle ->
            ValidationIssue(
                severity = ValidationSeverity.ERROR,
                code = ValidationErrorCode.WORKFLOW_DEP_CYCLE_DETECTED,
                message = "Workflow '$wid' has a dependency cycle: ${cycle.joinToString( " -> " )}.",
                path = "$wPath.steps",
                subjectId = wid
            )
        }
    }

    /**
     * Detects cycles in a directed acyclic graph using depth-first search.
     *
     * Uses a standard DFS-based algorithm with three states (unmarked, temporary, permanent)
     * to detect back-edges that indicate cycles. Returns one or more cycle paths found during
     * traversal. The algorithm processes nodes in sorted order for deterministic results.
     *
     * @param adj Adjacency list representing the dependency graph (node -> list of dependencies)
     * @return A list of detected cycles, where each cycle is a list of node IDs forming a cycle
     */
    private fun detectCycles( adj: Map<String, List<String>> ): List<List<String>>
    {
        val mark = mutableMapOf<String, Mark>()
        val stack = ArrayDeque<String>()
        val cycles = mutableListOf<List<String>>()

        fun visit( n: String )
        {
            when ( mark[n] )
            {
                Mark.PERM -> return
                Mark.TEMP ->
                {
                    // back-edge: extract cycle from stack
                    val cycle = stack.dropWhile { it != n } + n
                    cycles += cycle
                    return
                }
                null -> {}
            }

            mark[n] = Mark.TEMP
            stack.addLast( n )
            for ( m in adj[n].orEmpty() )
            {
                if ( m in adj ) visit( m ) // only consider nodes that exist in the workflow graph
            }
            stack.removeLast()
            mark[n] = Mark.PERM
        }

        // stable ordering for deterministic results
        for ( n in adj.keys.sorted() )
        {
            if ( mark[n] == null ) visit( n )
        }

        return cycles
    }
}
