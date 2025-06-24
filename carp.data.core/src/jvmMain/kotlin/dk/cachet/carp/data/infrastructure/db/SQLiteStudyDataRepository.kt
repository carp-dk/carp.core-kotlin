@file:Suppress("ImportOrdering")

package dk.cachet.carp.data.infrastructure.db

import dk.cachet.carp.common.application.NamespacedId
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Acceleration
import dk.cachet.carp.common.application.data.AngularVelocity
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.ECG
import dk.cachet.carp.common.application.data.Geolocation
import dk.cachet.carp.common.application.data.HeartRate
import dk.cachet.carp.common.application.data.StepCount
import dk.cachet.carp.data.application.CollectedDataPoint
import dk.cachet.carp.data.application.CollectedDataQuery
import dk.cachet.carp.data.application.DataStreamId
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlinx.datetime.Instant
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class SQLiteStudyDataRepository(
    jdbcUrl: String
) : StudyDataRepository
{

    private val connection: Connection = DriverManager.getConnection(jdbcUrl)

    override suspend fun queryData(
        studyId: UUID,
        query: CollectedDataQuery
    ): List<CollectedDataPoint>
    {
        val sqlBuilder = StringBuilder(
            """
            SELECT study_id, study_deployment_id, device_role_name,
                   data_type_namespace, data_type_name,
                   sensor_start_time, sensor_end_time, value
            FROM collected_data
            WHERE study_id = ?
            """.trimIndent()
        )

        val parameters = mutableListOf<Any>(studyId.toString())

        query.studyDeploymentIds?.takeIf { it.isNotEmpty() }?.let {
            sqlBuilder.append(" AND study_deployment_id IN (${it.joinToString(",") { "?" }})")
            parameters.addAll(it.map { id -> id.toString() })
        }

        query.deviceRoleNames?.takeIf { it.isNotEmpty() }?.let {
            sqlBuilder.append(" AND device_role_name IN (${it.joinToString(",") { "?" }})")
            parameters.addAll(it)
        }

        query.fields?.takeIf { it.isNotEmpty() }?.let {
            sqlBuilder.append(" AND data_type_name IN (${it.joinToString(",") { "?" }})")
            parameters.addAll(it)
        }

        query.from?.let {
            sqlBuilder.append(" AND sensor_start_time >= ?")
            parameters.add(it.toEpochMilliseconds())
        }

        query.to?.let {
            sqlBuilder.append(" AND sensor_start_time <= ?")
            parameters.add(it.toEpochMilliseconds())
        }

        sqlBuilder.append(" ORDER BY sensor_start_time ASC")


        val statement = connection.prepareStatement(sqlBuilder.toString())
        parameters.forEachIndexed { index, param ->
            statement.setObject(index + 1, param)
        }

        val resultSet = statement.executeQuery()

        val points = mutableListOf<CollectedDataPoint>()
        while (resultSet.next())
        {
            points.add(mapRowToCollectedDataPoint(resultSet))
        }

        return points
    }

    private fun mapRowToCollectedDataPoint( resultSet: ResultSet ): CollectedDataPoint
    {
        val studyDeploymentId = UUID(resultSet.getString("study_deployment_id"))
        val deviceRoleName = resultSet.getString("device_role_name")
        val namespace = resultSet.getString("data_type_namespace")
        val name = resultSet.getString("data_type_name")
        val timestamp = Instant.fromEpochMilliseconds(resultSet.getLong("sensor_start_time"))
        val valueJson = resultSet.getString("value")

        val dataType = NamespacedId(namespace, name)
        val streamId = DataStreamId(studyDeploymentId, deviceRoleName, dataType)

        val data: Data = deserializeData(name, valueJson)

        return CollectedDataPoint(
            streamId = streamId,
            timestamp = timestamp,
            data = data
        )
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val dataTypeMappings: Map<String, KClass<out Data>> = mapOf(
        "step_count" to StepCount::class,
        "heart_rate" to HeartRate::class,
        "eda" to ECG::class,
        "acceleration" to Acceleration::class,
        "angular_velocity" to AngularVelocity::class,
        "rotation" to AngularVelocity::class,
        "geolocation" to Geolocation::class,
    )

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun deserializeData( dataTypeName: String, valueJson: String ): Data
    {
        val targetClass = dataTypeMappings[dataTypeName]
            ?: throw IllegalArgumentException("Unsupported data type for deserialization: $dataTypeName")

        val serializer = targetClass.serializer() as kotlinx.serialization.KSerializer<Data>
        return json.decodeFromString(serializer, valueJson)
    }
}
