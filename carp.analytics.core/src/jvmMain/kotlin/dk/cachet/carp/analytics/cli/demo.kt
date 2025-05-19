package dk.cachet.carp.analytics.cli

import dk.cachet.carp.analytics.application.execution.SequentialExecutionStrategy
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.execution.ExecutorFactory
import dk.cachet.carp.analytics.application.process.DataRetrievalProcess
import dk.cachet.carp.analytics.application.process.MeanDailyStepCountProcess
import dk.cachet.carp.analytics.domain.workflow.Step
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

    val cwd = System.getProperty("user.dir")
    println("working in $cwd")
    println(File(cwd).listFiles()?.joinToString("\n") { it.name } ?: "Directory is empty or inaccessible")

    val yamlPath = ".\\src\\jvmTest\\resources\\demo_workflow.yaml"
    val yamlContent = File(yamlPath).readText()

    val workflowYaml = WorkflowYamlParser.fromString(yamlContent)


    println("Loaded workflow: ${workflowYaml.metadata.name}")

    // 2. Set up environment
    val dataRegistry = DataRegistry()
    val executionStrategy = SequentialExecutionStrategy(dataRegistry)
    val dbPath = "C:\\Users\\ngrec\\Code\\core_fork\\carp.core-kotlin\\carp.data.core\\src\\jvmTest\\resources\\test.db"
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
    val rebuiltSteps = workflow.getComponents().mapNotNull { component ->
        if (component !is Step) return@mapNotNull null
        val process = component.process

        when (process) {
            is DataRetrievalProcess -> {
                process.studyDataService = studyDataService
                process.dataRegistry = dataRegistry
                component.copy(process = process)
            }
            is MeanDailyStepCountProcess -> component // no injection needed
            else -> component
        }
    }

    val rebuiltWorkflow = Workflow(
        metadata = workflow.metadata.copy()
    )
    rebuiltWorkflow.addComponents(rebuiltSteps)
    return rebuiltWorkflow
}
