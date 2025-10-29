package dk.cachet.carp.analytics.application.process

import dk.cachet.carp.analytics.application.data.DataRegistry
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencies
import dk.cachet.carp.analytics.application.runtime.RuntimeDependencyKey
import dk.cachet.carp.analytics.domain.process.AnalysisProcess
import dk.cachet.carp.analytics.domain.process.InjectableProcess
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.CollectedDataQuery
import dk.cachet.carp.data.application.CollectedDataSet
import dk.cachet.carp.data.application.StudyDataService
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
* @param query Optional query parameters for filtering collected data.
 * @param name Name of the process, used for identification in workflows.
 * @param description Optional description of the process, providing additional context.
 */
@Serializable
@SerialName("data_retrieval")
class DataRetrievalProcess(
    override val name: String,
    override val description: String?,
    private val studyId: UUID,
    private val query: CollectedDataQuery? = null
) : AnalysisProcess, InjectableProcess
{

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
    override fun process( input: CollectedDataSet ): CollectedDataSet?
    {
        println("DataRetrievalProcess '$name': Querying study data service...")

        val result: CollectedDataSet = runBlocking {
            studyDataService.getCollectedData(
                studyId = studyId,
                query = CollectedDataQuery(
                    studyDeploymentIds = query?.studyDeploymentIds,
                    fields = query?.fields,
                    deviceRoleNames = query?.deviceRoleNames,
                    from = query?.from,
                    to = query?.to,
                    offsetDays = query?.offsetDays
                )
            )
        }

        println("DataRetrievalProcess '$name': Retrieved ${result.points.size} data points.")

        return result
    }

    /**
     * Injects required services used during execution.
     * Must be called before [process].
     */
    override fun inject( dependencies: Map<RuntimeDependencyKey<*>, Any> )
    {
        studyDataService = dependencies[RuntimeDependencies.StudyDataService] as? StudyDataService
            ?: error("StudyDataService is required")

        dataRegistry = dependencies[RuntimeDependencies.DataRegistry] as? DataRegistry
            ?: error("DataRegistry is required")
    }
}
