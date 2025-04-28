package dk.cachet.carp.analytics.cli

import dk.cachet.carp.analytics.application.execution.SequentialExecutionStrategy
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.execution.ExecutorFactory
import dk.cachet.carp.analytics.application.process.DataRetrievalProcess
import dk.cachet.carp.analytics.application.process.MeanDailyStepCountProcess
import dk.cachet.carp.analytics.domain.workflow.Workflow
import dk.cachet.carp.analytics.infrastructure.parser.WorkflowYamlParser
import dk.cachet.carp.data.application.StudyDataService
import dk.cachet.carp.data.infrastructure.db.DBBackedStudyDataService
import dk.cachet.carp.data.infrastructure.db.SQLiteStudyDataRepository
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    println("Starting demo CLI runner...")

    // 1. Load YAML
    val yamlPath = "D:\\Code\\carp.core-kotlin\\carp.analytics.core\\src\\jvmTest\\resources\\demo_workflow.yaml"
    val yamlContent = File(yamlPath).readText()

    val workflowYaml = WorkflowYamlParser.fromString(yamlContent)


    println("Loaded workflow: ${workflowYaml.name}")

    // 2. Set up environment
    val dataRegistry = DataRegistry()
    val executionStrategy = SequentialExecutionStrategy(dataRegistry)
    val dbPath = "D:\\Code\\carp.core-kotlin\\carp.data.core\\src\\jvmTest\\resources\\test.db"
    val jdbcUrl = "jdbc:sqlite:$dbPath"
    val repository = SQLiteStudyDataRepository(jdbcUrl)
    val studyDataService = DBBackedStudyDataService(repository )
    val rebuiltWorkflow = rebuildWorkflow(workflowYaml, studyDataService, dataRegistry)

    println(executionStrategy.toString())
    println(studyDataService.toString())

    //3 Execute the workflow
    executionStrategy.execute(
        rebuiltWorkflow,
        executorFactory = ExecutorFactory
    )


    println("Workflow completed successfully.")
}


fun rebuildWorkflow(
    workflow: Workflow,
    studyDataService: StudyDataService,
    dataRegistry: DataRegistry
): Workflow {
    val rebuiltSteps = workflow.getSteps().map { step ->
        val process = step.process

        when (process) {
            is DataRetrievalProcess -> {
                process.studyDataService = studyDataService
                process.dataRegistry = dataRegistry
                step.copy(process = process)
            }
            is MeanDailyStepCountProcess -> step // nothing special to inject
            else -> step
        }
    }

    val rebuiltWorkflow = Workflow(
        name = workflow.name,
        description = workflow.description
    )
    rebuiltWorkflow.addSteps(rebuiltSteps)
    return rebuiltWorkflow
}
