package dk.cachet.carp.data.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import kotlinx.serialization.*
import kotlin.js.JsExport


/**
 * Configures [expectedDataStreams] for a study deployment.
 */
@Serializable
@JsExport
data class DataStreamsConfiguration(
    val studyDeploymentId: UUID,
    val expectedDataStreams: Set<ExpectedDataStream>
)
{
    @Serializable
    data class ExpectedDataStream( val deviceRoleName: String, val dataType: DataType )
    {
        companion object
        {
            fun fromDataStreamId( dataStream: DataStreamId ) =
                ExpectedDataStream( dataStream.deviceRoleName, dataStream.dataType )
        }
    }

    /**
     * Get the [DataStreamId] for each of the [expectedDataStreams] in this configuration.
     */
    val expectedDataStreamIds: Set<DataStreamId>
        get() = expectedDataStreams.map { DataStreamId( studyDeploymentId, it.deviceRoleName, it.dataType ) }.toSet()
}
