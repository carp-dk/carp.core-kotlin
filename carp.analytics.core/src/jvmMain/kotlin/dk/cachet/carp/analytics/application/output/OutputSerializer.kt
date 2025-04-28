package dk.cachet.carp.analytics.application.output

import dk.cachet.carp.data.application.CollectedDataSet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.full.memberProperties

object OutputSerializer {

    fun serialize(
        dataSet: CollectedDataSet,
        fields: Set<String>? = null
    ): String {
        val outputMaps = dataSet.points.map { point ->
            val base = mutableMapOf<String, String>(
                "subject_id" to point.streamId.studyDeploymentId.toString(),
                "device_role" to point.streamId.deviceRoleName,
                "data_type" to point.streamId.dataType.name,
                "timestamp" to point.timestamp.toString()
            )

            val data = point.data
            val dataProperties = data::class.memberProperties.associate { prop ->
                val value = prop.getter.call(data)?.toString() ?: ""
                prop.name to value
            }

            val combined = base + dataProperties

            if (fields == null) {
                combined
            } else {
                combined.filterKeys { it in fields }
            }
        }

        return Json.encodeToString(outputMaps)
    }
}
