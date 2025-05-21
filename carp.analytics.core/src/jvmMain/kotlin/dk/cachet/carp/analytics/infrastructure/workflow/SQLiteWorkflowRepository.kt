package dk.cachet.carp.analytics.infrastructure.workflow

import dk.cachet.carp.analytics.application.WorkflowRepository
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import java.sql.*

class SQLiteWorkflowRepository(
    private val dbPath: String = "jdbc:sqlite:workflow.db"
) : WorkflowRepository {

    private fun connect(): Connection = DriverManager.getConnection(dbPath)

    override suspend fun create(metadata: StoredWorkflowMetadata): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("""
        INSERT INTO workflow_metadata (
            id, study_id, name, description, version_major, version_minor,
            file_path, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent())

        stmt.setString(1, metadata.id.toString())
        stmt.setString(2, metadata.studyId.toString())
        stmt.setString(3, metadata.name)
        stmt.setString(4, metadata.description)
        stmt.setInt(5, metadata.versionMajor)
        stmt.setObject(6, metadata.versionMinor)  // Nullable, so setObject instead of setInt
        stmt.setString(7, metadata.filePath)
        stmt.setString(8, metadata.createdAt.toString())
        stmt.setString(9, metadata.updatedAt.toString())

        return stmt.executeUpdate() > 0
    }


    override suspend fun update(metadata: StoredWorkflowMetadata): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            UPDATE workflow_metadata SET 
                name = ?, description = ?, version_major = ?, version_minor = ?,
                file_path = ?, updated_at = ?
            WHERE id = ? AND study_id = ?
        """.trimIndent())

        stmt.setString(1, metadata.name)
        stmt.setString(2, metadata.description)
        stmt.setInt(3, metadata.versionMajor)
        stmt.setObject(4, metadata.versionMinor)
        stmt.setString(5, metadata.filePath)
        stmt.setString(6, metadata.updatedAt.toString())
        stmt.setString(7, metadata.id.toString())
        stmt.setString(8, metadata.studyId.toString())

        return stmt.executeUpdate() > 0
    }

    override suspend fun findById(studyId: UUID, workflowId: UUID): StoredWorkflowMetadata? = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            SELECT * FROM workflow_metadata 
            WHERE id = ? AND study_id = ?
        """.trimIndent())

        stmt.setString(1, workflowId.toString())
        stmt.setString(2, studyId.toString())

        val rs = stmt.executeQuery()
        return if (rs.next()) fromResultSet(rs) else null
    }

    override suspend fun delete(studyId: UUID, workflowId: UUID): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("""
            DELETE FROM workflow_metadata WHERE id = ? AND study_id = ?
        """.trimIndent())

        stmt.setString(1, workflowId.toString())
        stmt.setString(2, studyId.toString())
        return stmt.executeUpdate() > 0
    }

    override suspend fun listByStudyId(studyId: UUID): List<StoredWorkflowMetadata> = connect().use { conn ->
        val sql = StringBuilder("""
            SELECT * FROM workflow_metadata WHERE study_id = ?
        """.trimIndent())

        val stmt = conn.prepareStatement(sql.toString())
        stmt.setString(1, studyId.toString())

        val rs = stmt.executeQuery()
        val results = mutableListOf<StoredWorkflowMetadata>()
        while (rs.next()) results.add(fromResultSet(rs))
        return results
    }

    private fun fromResultSet(rs: ResultSet): StoredWorkflowMetadata =
        StoredWorkflowMetadata(
            id = UUID.parse(rs.getString("id")),
            studyId = UUID.parse(rs.getString("study_id")),
            name = rs.getString("name"),
            description = rs.getString("description"),
            versionMajor = rs.getInt("version_major"),
            versionMinor = rs.getInt("version_minor"),
            filePath = rs.getString("file_path"),
            createdAt = Instant.parse(rs.getString("created_at")),
            updatedAt = Instant.parse(rs.getString("updated_at"))
        )
}
