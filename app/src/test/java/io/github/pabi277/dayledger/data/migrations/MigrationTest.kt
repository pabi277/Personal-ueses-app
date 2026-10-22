package io.github.pabi277.dayledger.data.migrations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

/**
 * Non-destructive upgrade proof for every supported start version.
 *
 * The tests run the exact SQL lists that [DatabaseMigrations] hands to Room, but
 * against real SQLite on the JVM (sqlite-jdbc), seeded with fictional rows. No
 * emulator, no owner data.
 */
class MigrationTest {

    @Before
    fun loadDriver() {
        Class.forName("org.sqlite.JDBC")
    }

    // ------------------------------------------------------------------ schemas

    /** The v1 schema as Room created it in Phase 1 (`fe58d9f`). */
    private fun createV1(conn: Connection) {
        conn.exec(
            "CREATE TABLE IF NOT EXISTS `app_usage` (`packageName` TEXT NOT NULL, `dateKey` TEXT NOT NULL, " +
                "`foregroundMinutes` INTEGER NOT NULL, `pickups` INTEGER NOT NULL, " +
                "`longestSessionMin` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`packageName`, `dateKey`))"
        )
        conn.exec(
            "CREATE TABLE IF NOT EXISTS `hourly_usage` (`dateKey` TEXT NOT NULL, `hour` INTEGER NOT NULL, " +
                "`minutes` INTEGER NOT NULL, PRIMARY KEY(`dateKey`, `hour`))"
        )
        conn.exec(
            "CREATE TABLE IF NOT EXISTS `app_info` (`packageName` TEXT NOT NULL, `label` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, `alwaysAllowed` INTEGER NOT NULL, `limitMin` INTEGER, " +
                "PRIMARY KEY(`packageName`))"
        )
        conn.exec(
            "CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`kind` TEXT NOT NULL, `budgetMin` INTEGER NOT NULL, `color` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        conn.exec("PRAGMA user_version = 1")
    }

    private fun upgradeTo(conn: Connection, from: Int) {
        DatabaseMigrations.statementsFrom(from).forEach { conn.exec(it) }
        conn.exec("PRAGMA user_version = ${DatabaseMigrations.CURRENT_VERSION}")
    }

    // ---------------------------------------------------------------- fixtures

    /** Fictional rows, deliberately not the owner's: numbers, unicode, nulls. */
    private fun seedV1(conn: Connection) {
        conn.exec("INSERT INTO categories (id, name, kind, budgetMin, color) VALUES ('study', 'Study', 'floor', 90, 4280391411)")
        conn.exec("INSERT INTO categories (id, name, kind, budgetMin, color) VALUES ('waste', 'Waste', 'ceiling', 45, 4294198070)")
        conn.exec("INSERT INTO app_info (packageName, label, category, alwaysAllowed, limitMin) VALUES ('com.example.reader', 'Example Reader', 'Study', 0, NULL)")
        conn.exec("INSERT INTO app_info (packageName, label, category, alwaysAllowed, limitMin) VALUES ('com.example.game', 'Example Game 🎮', 'Waste', 0, 30)")
        conn.exec("INSERT INTO app_usage (packageName, dateKey, foregroundMinutes, pickups, longestSessionMin, source, updatedAt) VALUES ('com.example.game', '2026-01-05', 62, 11, 18, 'device', 1767225600000)")
        conn.exec("INSERT INTO app_usage (packageName, dateKey, foregroundMinutes, pickups, longestSessionMin, source, updatedAt) VALUES ('com.example.reader', '2026-01-05', 45, 4, 25, 'device', 1767225600000)")
        conn.exec("INSERT INTO hourly_usage (dateKey, hour, minutes) VALUES ('2026-01-05', 21, 30)")
    }

    private fun seedV2Additions(conn: Connection) {
        conn.exec("INSERT INTO settings (key, value) VALUES ('dailyCap', '6')")
        conn.exec("INSERT INTO settings (key, value) VALUES ('quietWindow', '23:30-07:00')")
        conn.exec(
            "INSERT INTO notification_rules (id, type, enabled, times, days, scope, tone, templateText, quietFrom, quietTo, dailyCap) " +
                "VALUES ('evening', 'scheduled', 1, '21:00', '0,1,2,3,4,5,6', 'all', 'gentle', 'Today: {total} 🌙', '23:30', '07:00', 6)"
        )
        conn.exec("INSERT INTO notification_log (ruleId, firedAt, dateKey) VALUES ('evening', 1767225660000, '2026-01-05')")
    }

    private fun seedV3Additions(conn: Connection) {
        conn.exec("INSERT INTO templates (id, name, lastUsedAt) VALUES ('college', 'College day', 1767225600000)")
        conn.exec("INSERT INTO template_items (templateId, startMin, endMin, title) VALUES ('college', 540, 600, 'Lecture')")
        conn.exec("INSERT INTO day_plans (dateKey, templateId, note) VALUES ('2026-01-05', 'college', 'exam week')")
        conn.exec("INSERT INTO blocks (dateKey, startMin, endMin, title, kind, reminder) VALUES ('2026-01-05', 540, 600, 'Lecture', 'soft', 1)")
    }

    private fun seedV4Additions(conn: Connection) {
        conn.exec(
            "INSERT INTO limit_events (dateKey, packageName, limitMin, reachedAt, action, blockDurationMin, unlockedAt, overrideType) " +
                "VALUES ('2026-01-05', 'com.example.game', 30, 1767226000000, 'block', 30, 0, 'none')"
        )
    }

    private fun seedV5Additions(conn: Connection) {
        conn.exec(
            "INSERT INTO earnings (dateKey, source, taskType, appId, amountPaise, loggedAt, attributedMinutes) " +
                "VALUES ('2026-01-05', 'ExampleOffers', 'install', 'com.example.other', 1250, 1767226000000, 24)"
        )
        conn.exec("INSERT INTO learning (dateKey, minutes, text, createdAt) VALUES ('2026-01-05', 25, 'Read two chapters', 1767226200000)")
    }

    private fun seedThrough(conn: Connection, version: Int) {
        seedV1(conn)
        if (version >= 2) seedV2Additions(conn)
        if (version >= 3) seedV3Additions(conn)
        if (version >= 4) seedV4Additions(conn)
        if (version >= 5) seedV5Additions(conn)
    }

    private fun connect(): Connection = DriverManager.getConnection("jdbc:sqlite::memory:")

    private fun Connection.exec(sql: String) {
        createStatement().use { it.execute(sql) }
    }

    private fun Connection.tableNames(): List<String> {
        val names = ArrayList<String>()
        createStatement().use { statement ->
            statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
            ).use { rs -> while (rs.next()) names.add(rs.getString(1)) }
        }
        return names
    }

    private fun Connection.count(table: String): Int {
        createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM `$table`").use { rs ->
                rs.next()
                return rs.getInt(1)
            }
        }
    }

    /** name → "TYPE|notnull|pk" for every column, for exact schema comparison. */
    private fun Connection.columns(table: String): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        createStatement().use { statement ->
            statement.executeQuery("PRAGMA table_info(`$table`)").use { rs ->
                while (rs.next()) {
                    val name = rs.getString("name")
                    val type = rs.getString("type").uppercase()
                    val notNull = rs.getInt("notnull") != 0
                    val pk = rs.getInt("pk")
                    result[name] = "$type|${if (notNull) 1 else 0}|$pk"
                }
            }
        }
        return result
    }

    // ------------------------------------------------------------------- tests

    @Test
    fun `every step exists with the expected start and end versions`() {
        assertEquals(listOf(1 to 2, 2 to 3, 3 to 4, 4 to 5), DatabaseMigrations.STEPS.map { it.from to it.to })
        assertEquals(4, DatabaseMigrations.ALL.size)
        assertEquals(5, DatabaseMigrations.CURRENT_VERSION)
        assertEquals(listOf(1, 2, 3, 4, 5), DatabaseMigrations.SUPPORTED_VERSIONS)
    }

    @Test
    fun `no migration statement is destructive`() {
        val forbidden = listOf("DROP TABLE", "DROP COLUMN", "DELETE FROM", "TRUNCATE", "RENAME TO")
        DatabaseMigrations.STEPS.forEach { step ->
            step.statements.forEach { sql ->
                val upper = sql.uppercase()
                forbidden.forEach { word ->
                    assertTrue("${step.from}->${step.to} statement contains $word: $sql", !upper.contains(word))
                }
            }
        }
    }

    @Test
    fun `1 to 2 keeps every v1 row`() {
        val conn = connect()
        conn.use {
            createV1(it)
            seedV1(it)
            DatabaseMigrations.V1_TO_V2.forEach { sql -> it.exec(sql) }
            assertEquals(2, it.count("app_usage"))
            assertEquals(1, it.count("hourly_usage"))
            assertEquals(2, it.count("app_info"))
            assertEquals(2, it.count("categories"))
            assertEquals(0, it.count("settings"))
            assertEquals(0, it.count("notification_rules"))
            assertEquals(0, it.count("notification_log"))
        }
    }

    @Test
    fun `1 to 3 keeps every v1 and v2 row`() {
        val conn = connect()
        conn.use {
            createV1(it)
            seedV1(it)
            DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
            seedV2Additions(it)
            assertEquals(2, it.count("app_usage"))
            assertEquals(2, it.count("settings"))
            assertEquals(1, it.count("notification_log"))
            assertEquals(0, it.count("blocks"))
        }
    }

    @Test
    fun `every supported start version reaches v5 with all its rows intact`() {
        for (start in DatabaseMigrations.SUPPORTED_VERSIONS) {
            val conn = connect()
            conn.use {
                createV1(it)
                // Bring the schema to the start version, then seed that version's data.
                when (start) {
                    1 -> Unit
                    2 -> DatabaseMigrations.V1_TO_V2.forEach { sql -> it.exec(sql) }
                    3 -> DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
                    4 -> DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
                    5 -> DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
                }
                seedThrough(it, start)

                val before = HashMap<String, Int>()
                it.tableNames().forEach { table -> before[table] = it.count(table) }

                val remaining = DatabaseMigrations.statementsFrom(start)
                remaining.forEach { sql -> it.exec(sql) }
                it.exec("PRAGMA user_version = 5")

                assertEquals("start v$start: user_version", 5, it.readUserVersion())
                assertEquals(
                    "start v$start: table set",
                    setOf(
                        "app_info", "app_usage", "blocks", "categories", "day_plans", "earnings",
                        "hourly_usage", "learning", "limit_events", "notification_log",
                        "notification_rules", "settings", "template_items", "templates"
                    ),
                    it.tableNames().toSet()
                )
                before.forEach { (table, rows) ->
                    assertEquals("start v$start: $table rows survived", rows, it.count(table))
                }
            }
        }
    }

    @Test
    fun `migrated schema matches the v5 entity definitions exactly`() {
        val conn = connect()
        conn.use {
            createV1(it)
            DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
            it.exec("PRAGMA user_version = 5")

            // Expectations mirror the current entities: TEXT/INTEGER, nullability
            // and primary-key position. Any drift fails this test.
            val expected = mapOf(
                "app_usage" to listOf("packageName|TEXT|1|1", "dateKey|TEXT|1|2", "foregroundMinutes|INTEGER|1|0",
                    "pickups|INTEGER|1|0", "longestSessionMin|INTEGER|1|0", "source|TEXT|1|0", "updatedAt|INTEGER|1|0"),
                "hourly_usage" to listOf("dateKey|TEXT|1|1", "hour|INTEGER|1|2", "minutes|INTEGER|1|0"),
                "app_info" to listOf("packageName|TEXT|1|1", "label|TEXT|1|0", "category|TEXT|1|0",
                    "alwaysAllowed|INTEGER|1|0", "limitMin|INTEGER|0|0"),
                "categories" to listOf("id|TEXT|1|1", "name|TEXT|1|0", "kind|TEXT|1|0", "budgetMin|INTEGER|1|0", "color|INTEGER|1|0"),
                "settings" to listOf("key|TEXT|1|1", "value|TEXT|1|0"),
                "notification_rules" to listOf("id|TEXT|1|1", "type|TEXT|1|0", "enabled|INTEGER|1|0", "times|TEXT|1|0",
                    "days|TEXT|1|0", "scope|TEXT|1|0", "tone|TEXT|1|0", "templateText|TEXT|1|0",
                    "quietFrom|TEXT|1|0", "quietTo|TEXT|1|0", "dailyCap|INTEGER|1|0"),
                "notification_log" to listOf("id|INTEGER|1|1", "ruleId|TEXT|1|0", "firedAt|INTEGER|1|0", "dateKey|TEXT|1|0"),
                "blocks" to listOf("id|INTEGER|1|1", "dateKey|TEXT|1|0", "startMin|INTEGER|1|0", "endMin|INTEGER|1|0",
                    "title|TEXT|1|0", "kind|TEXT|1|0", "reminder|INTEGER|1|0"),
                "day_plans" to listOf("dateKey|TEXT|1|1", "templateId|TEXT|0|0", "note|TEXT|1|0"),
                "templates" to listOf("id|TEXT|1|1", "name|TEXT|1|0", "lastUsedAt|INTEGER|1|0"),
                "template_items" to listOf("id|INTEGER|1|1", "templateId|TEXT|1|0", "startMin|INTEGER|1|0",
                    "endMin|INTEGER|1|0", "title|TEXT|1|0"),
                "limit_events" to listOf("id|INTEGER|1|1", "dateKey|TEXT|1|0", "packageName|TEXT|1|0", "limitMin|INTEGER|1|0",
                    "reachedAt|INTEGER|1|0", "action|TEXT|1|0", "blockDurationMin|INTEGER|1|0",
                    "unlockedAt|INTEGER|1|0", "overrideType|TEXT|1|0"),
                "earnings" to listOf("id|INTEGER|1|1", "dateKey|TEXT|1|0", "source|TEXT|1|0", "taskType|TEXT|1|0",
                    "appId|TEXT|0|0", "amountPaise|INTEGER|1|0", "loggedAt|INTEGER|1|0", "attributedMinutes|INTEGER|1|0"),
                "learning" to listOf("id|INTEGER|1|1", "dateKey|TEXT|1|0", "minutes|INTEGER|1|0", "text|TEXT|1|0",
                    "createdAt|INTEGER|1|0")
            )

            // Report every difference in one go: a single CI run then shows the
            // complete picture instead of one table at a time.
            val problems = ArrayList<String>()
            expected.forEach { (table, columns) ->
                val actualList = it.columns(table).entries.map { (name, spec) -> "$name|$spec" }
                if (actualList != columns) {
                    problems.add("table $table\n  expected: ${columns.joinToString(", ")}\n  actual:   ${actualList.joinToString(", ")}")
                }
            }
            val actualTables = it.tableNames().toSet()
            if (actualTables != expected.keys) {
                problems.add("tables\n  expected: ${expected.keys.sorted()}\n  actual:   ${actualTables.sorted()}")
            }
            assertTrue("schema drift from the v5 entities:\n" + problems.joinToString("\n"), problems.isEmpty())
        }
    }

    @Test
    fun `migrations do not touch rows that already exist`() {
        val conn = connect()
        conn.use {
            createV1(it)
            seedV1(it)
            val before = it.count("app_usage")
            DatabaseMigrations.statementsFrom(1).forEach { sql -> it.exec(sql) }
            assertEquals(before, it.count("app_usage"))
            assertGameMinutesStillCorrect(it)
        }
    }

    private fun assertGameMinutesStillCorrect(conn: Connection) {
        conn.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT foregroundMinutes FROM app_usage WHERE packageName = 'com.example.game' AND dateKey = '2026-01-05'"
            ).use { rs ->
                assertTrue(rs.next())
                assertEquals(62, rs.getInt(1))
            }
        }
    }

    private fun Connection.readUserVersion(): Int =
        createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
}
