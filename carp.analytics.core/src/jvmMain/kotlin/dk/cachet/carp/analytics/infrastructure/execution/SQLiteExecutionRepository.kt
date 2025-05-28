package dk.cachet.carp.analytics.infrastructure.execution

import dk.cachet.carp.analytics.application.ExecutionRepository
import dk.cachet.carp.analytics.domain.data.ExecutionOutput
import dk.cachet.carp.analytics.domain.execution.ArtifactType
import dk.cachet.carp.analytics.domain.execution.BasicExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionArtifact
import dk.cachet.carp.analytics.domain.execution.ExecutionResult
import dk.cachet.carp.analytics.domain.execution.ExecutionState
import dk.cachet.carp.analytics.domain.execution.ExecutionStatus
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.sql.*

class SQLiteExecutionRepository(
    private val dbPath: String = "jdbc:sqlite:execution.db"
) : ExecutionRepository {

    private fun connect(): Connection = DriverManager.getConnection(dbPath)

    override suspend fun saveState(state: ExecutionState): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            INSERT INTO execution_state (
                execution_id, study_id, workflow_id, status, start_time, end_time
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent())

        stmt.setString(1, state.executionId.toString())
        stmt.setString(2, state.studyId.toString())
        stmt.setString(3, state.workflowId.toString())
        stmt.setString(4, state.status.name)
        stmt.setString(5, state.startedAt.toString())
        stmt.setString(6, state.completedAt?.toString())
        stmt.executeUpdate() > 0
    }

    override suspend fun updateState(state: ExecutionState): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            UPDATE execution_state SET
                status = ?, start_time = ?, end_time = ?, study_id = ?, workflow_id = ?
            WHERE execution_id = ?
        """.trimIndent())

        stmt.setString(1, state.status.name)
        stmt.setString(2, state.startedAt.toString())
        stmt.setString(3, state.completedAt?.toString())
        stmt.setString(4, state.studyId.toString())
        stmt.setString(5, state.workflowId.toString())
        stmt.setString(6, state.executionId.toString())
        stmt.executeUpdate() > 0
    }

    override suspend fun getState(executionId: UUID): ExecutionState? = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            SELECT * FROM execution_state WHERE execution_id = ?
        """.trimIndent())

        stmt.setString(1, executionId.toString())
        val rs = stmt.executeQuery()
        return if (rs.next()) fromResultSet(rs) else null
    }

    override suspend fun getLatestStatus(workflowId: UUID): ExecutionState? = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            SELECT * FROM execution_state 
            WHERE workflow_id = ?
            ORDER BY start_time DESC LIMIT 1
        """.trimIndent())

        stmt.setString(1, workflowId.toString())
        val rs = stmt.executeQuery()
        return if (rs.next()) fromResultSet(rs) else null
    }

    override suspend fun findByStudy(
        studyId: UUID,
        workflowId: UUID?,
        from: Instant?,
        to: Instant?
    ): List<ExecutionState> = connect().use { conn ->
        val query = StringBuilder("""
            SELECT * FROM execution_state
            WHERE study_id = ?
        """)
        if (workflowId != null) query.append(" AND workflow_id = ?")
        if (from != null) query.append(" AND start_time >= ?")
        if (to != null) query.append(" AND start_time <= ?")
        query.append(" ORDER BY start_time DESC")

        val stmt = conn.prepareStatement(query.toString())

        var i = 1
        stmt.setString(i++, studyId.toString())
        if (workflowId != null) stmt.setString(i++, workflowId.toString())
        if (from != null) stmt.setString(i++, from.toString())
        if (to != null) stmt.setString(i++, to.toString())

        val rs = stmt.executeQuery()
        val results = mutableListOf<ExecutionState>()
        while (rs.next()) results.add(fromResultSet(rs))
        return results
    }

    override suspend fun saveResult(result: ExecutionResult): Boolean = connect().use { conn ->
        conn.autoCommit = false
        try {
            val insertResult = conn.prepareStatement("""
            INSERT INTO execution_result (execution_id, status, outputs_json)
            VALUES (?, ?, ?)
            ON CONFLICT(execution_id) DO UPDATE SET
                status = excluded.status,
                outputs_json = excluded.outputs_json
        """.trimIndent())

            insertResult.setString(1, result.executionId.toString())
            insertResult.setString(2, result.status.name)
            insertResult.setString(3, Json.encodeToString(result.outputs ?: emptyList()))
            insertResult.executeUpdate()

            val deleteArtifacts = conn.prepareStatement("""
            DELETE FROM execution_artifact WHERE execution_id = ?
        """.trimIndent())
            deleteArtifacts.setString(1, result.executionId.toString())
            deleteArtifacts.executeUpdate()

            if (result is BasicExecutionResult) {
                val insertArtifact = conn.prepareStatement("""
                INSERT INTO execution_artifact (execution_id, uri, name, type, mime_type)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent())

                result.artifacts.forEach { artifact ->
                    insertArtifact.setString(1, result.executionId.toString())
                    insertArtifact.setString(2, artifact.uri)
                    insertArtifact.setString(3, artifact.name)
                    insertArtifact.setString(4, artifact.type.name)
                    insertArtifact.setString(5, artifact.mimeType)
                    insertArtifact.addBatch()
                }
                insertArtifact.executeBatch()
            }

            conn.commit()
            return true
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }


    override suspend fun getResult(executionId: UUID): ExecutionResult? = connect().use { conn ->
        val resultStmt = conn.prepareStatement("""
        SELECT * FROM execution_result WHERE execution_id = ?
    """.trimIndent())

        resultStmt.setString(1, executionId.toString())
        val rs = resultStmt.executeQuery()

        if (!rs.next()) return null

        val outputs: List<ExecutionOutput>? = rs.getString("outputs_json")?.let { Json.decodeFromString(it) }
        val status = ExecutionStatus.valueOf(rs.getString("status"))

        val artifactsStmt = conn.prepareStatement("""
        SELECT * FROM execution_artifact WHERE execution_id = ?
    """.trimIndent())
        artifactsStmt.setString(1, executionId.toString())
        val ars = artifactsStmt.executeQuery()

        val artifacts = mutableListOf<ExecutionArtifact>()
        while (ars.next()) {
            artifacts.add(
                ExecutionArtifact(
                    uri = ars.getString("uri"),
                    name = ars.getString("name"),
                    type = ArtifactType.valueOf(ars.getString("type")),
                    mimeType = ars.getString("mime_type")
                )
            )
        }

        return BasicExecutionResult(
            executionId = executionId,
            status = status,
            outputs = outputs,
            artifacts = artifacts
        )
    }


    private fun fromResultSet(rs: ResultSet): ExecutionState =
        ExecutionState(
            UUID.parse(rs.getString("execution_id")),
            ExecutionStatus.valueOf(rs.getString("status")),
            Instant.parse(rs.getString("start_time")),
            rs.getString("end_time")?.let { Instant.parse(it) },
            UUID.parse(rs.getString("workflow_id")),
            UUID.parse(rs.getString("study_id"))
        )
}
