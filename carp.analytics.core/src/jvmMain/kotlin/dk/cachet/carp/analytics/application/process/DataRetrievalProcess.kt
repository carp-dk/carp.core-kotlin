package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencies
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencyKey
import dk.cachet.carp.analytics.domain.process.InjectableProcess
import dk.cachet.carp.data.application.StudyDataService
import kotlinx.datetime.Instant
import dk.cachet.carp.common.application.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A process that retrieves collected data from a [StudyDataService] and returns it for use
 * in a workflow step. This is typically the first step in a workflow that needs study data.
 *
 * It supports filtering by deployment ID, device roles, fields, time range, and offset days.
 * All results are optionally registered in a [DataRegistry] if needed for downstream use.
 *
 * This process must be injected with runtime dependencies before execution via [inject] method.
 *
 * @param studyId ID of the study to retrieve data for.
 * @param deploymentIds Optional filter to specific deployments within the study.
 * @param fields Optional filter to specific data types.
 * @param deviceRoles Optional filter by device role name.
 * @param from Optional start time.
 * @param to Optional end time.
 * @param offsetDays Optional time offset in days relative to now.
 */
@Serializable
@SerialName("data_retrieval")
class DataRetrievalProcess(
    override val name: String,
    override val description: String?,
    private val studyId: UUID,
    private val deploymentIds: Set<UUID>? = null,
    private val fields: Set<String>? = null,
    private val deviceRoles: Set<String>? = null,
    private val from: Instant? = null,
    private val to: Instant? = null,
    private val offsetDays: Int? = null,
) : AnalysisProcess, InjectableProcess {

    /**
     * Must be injected before execution. Provides access to the study’s collected data.
     */
    @Transient
    lateinit var studyDataService: StudyDataService

    /**
     * Shared registry used to publish outputs for downstream steps.
     */
    @Transient
    lateinit var dataRegistry: DataRegistry

    /**
     * Fetches the requested data from the study and returns it.
     *
     * @param input Not used in this process; included to satisfy the [AnalysisProcess] interface.
     * @return The dataset retrieved from the study.
     */
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

        return result
    }

    /**
     * Injects required services used during execution.
     * Must be called before [process].
     */
    override fun inject(dependencies: Map<RuntimeDependencyKey<*>, Any>) {
        studyDataService = dependencies[RuntimeDependencies.StudyDataService] as? StudyDataService
            ?: throw IllegalStateException("StudyDataService is required")

        dataRegistry = dependencies[RuntimeDependencies.DataRegistry] as? DataRegistry
            ?: throw IllegalStateException("DataRegistry is required")
    }
}
