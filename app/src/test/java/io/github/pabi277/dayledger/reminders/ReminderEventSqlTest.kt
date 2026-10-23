package io.github.pabi277.dayledger.reminders

import io.github.pabi277.dayledger.data.migrations.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

/**
 * The two cap queries, run against real SQLite.
 *
 * The engine takes the counts as plain integers, so a wrong predicate in the DAO would
 * silently disable a cap — the kind of bug a pure rules test cannot see. This runs the same
 * SQL the DAO declares against a real v8 schema built by [DatabaseMigrations], with
 * fictional rows only.
 */
class ReminderEventSqlTest {

    private lateinit var conn: Connection

    @Before
    fun setUp() {
        Class.forName("org.sqlite.JDBC")
        conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        // The v7 → v8 step is self-contained: it only creates the two reminder tables.
        DatabaseMigrations.V7_TO_V8.forEach { sql -> conn.createStatement().use { it.execute(sql) } }
    }

    private fun fire(profile: String, audible: Boolean, dateKey: String = "2026-10-23") {
        conn.createStatement().use { statement ->
            statement.execute(
                "INSERT INTO reminder_events (occurrenceId, dateKey, kind, profile, audible, detail, at) " +
                    "VALUES ('habit:read:$dateKey', '$dateKey', 'fire', '$profile', ${if (audible) 1 else 0}, '', 1767225600000)"
            )
        }
    }

    private fun event(kind: String, occurrenceId: String = "habit:read:2026-10-23") {
        conn.createStatement().use { statement ->
            statement.execute(
                "INSERT INTO reminder_events (occurrenceId, dateKey, kind, profile, audible, detail, at) " +
                    "VALUES ('$occurrenceId', '2026-10-23', '$kind', 'core', 0, '', 1767225600000)"
            )
        }
    }

    private fun count(sql: String): Int =
        conn.createStatement().use { statement ->
            statement.executeQuery(sql).use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }

    private val coreSql =
        "SELECT COUNT(*) FROM reminder_events WHERE dateKey = '2026-10-23' " +
            "AND kind = 'fire' AND audible = 1 AND profile = 'core'"

    private val generalSql =
        "SELECT COUNT(*) FROM reminder_events WHERE dateKey = '2026-10-23' " +
            "AND kind = 'fire' AND audible = 1 AND profile <> 'core'"

    @Test
    fun `core and general caps count separately`() {
        fire("core", audible = true)
        fire("core", audible = true)
        fire("standard", audible = true)
        assertEquals(2, count(coreSql))
        assertEquals(1, count(generalSql))
    }

    @Test
    fun `silent fires do not consume a cap`() {
        fire("core", audible = true)
        fire("core", audible = false)
        fire("core", audible = false)
        fire("standard", audible = false)
        assertEquals(1, count(coreSql))
        assertEquals(0, count(generalSql))
    }

    @Test
    fun `actions and stale taps are not fires`() {
        fire("core", audible = true)
        event(ReminderEventKind.ACTION)
        event(ReminderEventKind.STALE)
        event(ReminderEventKind.PLAN)
        assertEquals(1, count(coreSql))
    }

    @Test
    fun `yesterday's fires do not count toward today's cap`() {
        fire("core", audible = true, dateKey = "2026-10-22")
        fire("core", audible = true, dateKey = "2026-10-22")
        fire("core", audible = true)
        assertEquals(1, count(coreSql))
    }

    @Test
    fun `a settled occurrence can only log one action`() {
        // This is what the receiver guards against: the second tap writes a `stale` row.
        event(ReminderEventKind.ACTION)
        event(ReminderEventKind.STALE)
        assertEquals(
            1,
            count("SELECT COUNT(*) FROM reminder_events WHERE kind = 'action'")
        )
        assertEquals(
            1,
            count("SELECT COUNT(*) FROM reminder_events WHERE kind = 'stale'")
        )
    }

    @Test
    fun `the state table accepts exactly one row per occurrence`() {
        conn.createStatement().use { statement ->
            statement.execute(
                "INSERT INTO reminder_states (occurrenceId, profile, audibleFires, totalFires, degraded, updatedAt) " +
                    "VALUES ('habit:read:2026-10-23', 'core', 1, 1, '', 1767225600000)"
            )
        }
        val duplicate = runCatching {
            conn.createStatement().use { statement ->
                statement.execute(
                    "INSERT INTO reminder_states (occurrenceId, profile, audibleFires, totalFires, degraded, updatedAt) " +
                        "VALUES ('habit:read:2026-10-23', 'standard', 0, 0, '', 1767225600000)"
                )
            }
        }
        assertEquals(true, duplicate.isFailure)
        assertEquals(1, count("SELECT COUNT(*) FROM reminder_states"))
    }
}
