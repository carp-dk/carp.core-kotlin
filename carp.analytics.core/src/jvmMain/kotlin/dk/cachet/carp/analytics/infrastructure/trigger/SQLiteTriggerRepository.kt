package dk.cachet.carp.analytics.infrastructure.trigger

import dk.cachet.carp.analytics.application.TriggerRepository
import dk.cachet.carp.analytics.domain.trigger.*
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Types

class SqliteTriggerRepository(
    private val dbPath: String
) : TriggerRepository {

    private fun connect(): Connection = DriverManager.getConnection(dbPath)

    override suspend fun create(trigger: Trigger): Trigger = connect().use { conn ->
        val stmt = conn.prepareStatement(
            """
            INSERT INTO Triggers (id, study_id, workflow_id, name, created_at, type, cron_expr, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        )

        stmt.setString(1, trigger.id.toString())
        stmt.setString(2, trigger.studyId.toString())
        stmt.setString(3, trigger.workflowId.toString())
        stmt.setString(4, trigger.name)
        stmt.setString(5, trigger.createdAt.toString())

        when (trigger) {
            is ManualTrigger -> {
                stmt.setString(6, "manual")
                stmt.setNull(7, Types.VARCHAR)
                stmt.setNull(8, Types.VARCHAR)
            }
            is ScheduledTrigger -> {
                stmt.setString(6, "scheduled")
                stmt.setString(7, trigger.cron.expression)
                stmt.setString(8, trigger.updatedAt?.toString())
            }
        }
        stmt.executeUpdate()
        trigger
    }

    override suspend fun update(trigger: Trigger): Trigger = connect().use { conn ->
        val stmt = conn.prepareStatement(
            """
            UPDATE Triggers SET name = ?, study_id = ?, workflow_id = ?, type = ?, cron_expr = ?, updated_at = ?
            WHERE id = ?
            """.trimIndent()
        )

        stmt.setString(1, trigger.name)
        stmt.setString(2, trigger.studyId.toString())
        stmt.setString(3, trigger.workflowId.toString())

        when (trigger) {
            is ManualTrigger -> {
                stmt.setString(4, "manual")
                stmt.setNull(5, Types.VARCHAR)
                stmt.setNull(6, Types.VARCHAR)
            }
            is ScheduledTrigger -> {
                stmt.setString(4, "scheduled")
                stmt.setString(5, trigger.cron.expression)
                stmt.setString(6, trigger.updatedAt?.toString())
            }
        }

        stmt.setString(7, trigger.id.toString())
        stmt.executeUpdate()
        trigger
    }

    override suspend fun delete(triggerId: UUID): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement("DELETE FROM Triggers WHERE id = ?")
        stmt.setString(1, triggerId.toString())
        stmt.executeUpdate() > 0
    }

    override suspend fun get(triggerId: UUID): Trigger? = connect().use { conn ->
        val stmt = conn.prepareStatement("SELECT * FROM Triggers WHERE id = ?")
        stmt.setString(1, triggerId.toString())
        val rs = stmt.executeQuery()
        if (rs.next()) rs.toTrigger() else null
    }


    override suspend fun list(studyId: UUID): List<Trigger> = connect().use { conn ->
        val stmt = conn.prepareStatement("SELECT * FROM Triggers WHERE study_id = ?")
        stmt.setString(1, studyId.toString())
        val rs = stmt.executeQuery()
        generateSequence { if (rs.next()) rs.toTrigger() else null }.toList()
    }

    override suspend fun listByWorkflow(studyId: UUID, workflowId: UUID): List<Trigger> = connect().use { conn ->
        val stmt = conn.prepareStatement("SELECT * FROM Triggers WHERE study_id = ? AND workflow_id = ?")
        stmt.setString(1, studyId.toString())
        stmt.setString(2, workflowId.toString())
        val rs = stmt.executeQuery()
        generateSequence { if (rs.next()) rs.toTrigger() else null }.toList()
    }

    override suspend fun startTrigger(triggerId: UUID, startedAt: Instant): Boolean {
        val trigger = get(triggerId) ?: return false
        val activation = TriggerActivation(
            UUID.randomUUID(), triggerId, trigger.studyId, startedAt, null
        )
        return recordActivation(activation)
    }

    override suspend fun recordActivation(activation: TriggerActivation): Boolean = connect().use { conn ->
        val stmt = conn.prepareStatement(
            """
            INSERT INTO TriggerActivations (id, trigger_id, study_id, fired_at, workflow_exec_id)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
        )
        stmt.setString(1, activation.id.toString())
        stmt.setString(2, activation.triggerId.toString())
        stmt.setString(3, activation.studyId.toString())
        stmt.setString(4, activation.firedAt.toString())
        if (activation.workflowExecutionId != null)
            stmt.setString(5, activation.workflowExecutionId.toString())
        else stmt.setNull(5, Types.VARCHAR)

        stmt.executeUpdate() > 0
    }

    override suspend fun getActivationsForStudy(studyId: UUID): List<TriggerActivation> = connect().use { conn ->
        val stmt = conn.prepareStatement("SELECT * FROM TriggerActivations WHERE study_id = ?")
        stmt.setString(1, studyId.toString())
        val rs = stmt.executeQuery()
        generateSequence { if (rs.next()) rs.toTriggerActivation() else null }.toList()
    }

    override suspend fun getActivationsForTrigger(triggerId: UUID): List<TriggerActivation> = connect().use { conn ->
        val stmt = conn.prepareStatement("SELECT * FROM TriggerActivations WHERE trigger_id = ?")
        stmt.setString(1, triggerId.toString())
        val rs = stmt.executeQuery()
        generateSequence { if (rs.next()) rs.toTriggerActivation() else null }.toList()
    }

    private fun ResultSet.toTrigger(): Trigger {
        val type = getString("type")
        val id = UUID(getString("id"))
        val studyId = UUID(getString("study_id"))
        val workflowId = UUID(getString("workflow_id"))
        val name = getString("name")
        val createdAt = Instant.parse(getString("created_at"))

        return when (type) {
            "manual" -> ManualTrigger(id, studyId, workflowId, name, createdAt)
            "scheduled" -> ScheduledTrigger(
                id, studyId, workflowId, name,
                CronExpression(getString("cron_expr")), createdAt,
                getString("updated_at")?.let(Instant::parse)
            )
            else -> error("Unknown trigger type: $type")
        }
    }

    private fun ResultSet.toTriggerActivation(): TriggerActivation = TriggerActivation(
        UUID(getString("id")),
        UUID(getString("trigger_id")),
        UUID(getString("study_id")),
        Instant.parse(getString("fired_at")),
        getString("workflow_exec_id")?.let{UUID(it)}
    )
}
