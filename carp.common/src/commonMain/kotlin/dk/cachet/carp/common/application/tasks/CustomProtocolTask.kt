package dk.cachet.carp.common.application.tasks

import dk.cachet.carp.common.application.data.NoData
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * A [TaskConfiguration] which contains a definition on how to run tasks, measures, and triggers which differs from the CARP domain model.
 */
@Serializable
@JsExport
data class CustomProtocolTask(
    override val name: String,
    /**
     * A definition on how to run a study on a primary device, serialized as a string.
     */
    val studyProtocol: String
) : TaskConfiguration<NoData>
{
    /**
     * Description is empty, since it is likely defined in [studyProtocol] in a different format.
     */
    override val description: String? = null

    /**
     * This list is empty, since measures are defined in [studyProtocol] in a different format.
     */
    override val measures: List<Measure> = emptyList()
}
