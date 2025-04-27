package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.data.InMemoryData
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Instant
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.runBlocking

/**
 * A process that retrieves data from a StudyDataService and registers it into the DataRegistry.
 */
class DataRetrievalProcess(
    override val name: String,
    override val description: String,
    private val studyId: UUID,
    private val studyDataService: StudyDataService,
    private val outputName: String,
    private val deploymentIds: Set<UUID>? = null,
    private val fields: Set<String>? = null,
    private val deviceRoles: Set<String>? = null,
    private val from: Instant? = null,
    private val to: Instant? = null,
    private val offsetDays: Int? = null,
    private val dataRegistry: DataRegistry
) : AnalysisProcess {

    override fun process(input: CollectedDataSet): CollectedDataSet? {
        println("DataRetrievalProcess '$name': Querying study data service...")

        val result: CollectedDataSet = runBlocking {
            studyDataService.getCollectedData(
                studyId = studyId,
                studyDeploymentIds = deploymentIds,
                fields = fields,
                deviceRoleNames = deviceRoles,
                from = from,
                to = to,
                offsetDays = offsetDays
            )
        }

        println("DataRetrievalProcess '$name': Retrieved ${result.points.size} data points.")

        dataRegistry.register(outputName, InMemoryData(result))

        return result
    }
}
