package io.github.pabi277.dayledger.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

/**
 * Regression test for the empty `app_info` table found in the owner's real backup on
 * 2026-10-23 (0 app_info rows against 301 app_usage rows).
 *
 * `alwaysAllowed` is `INTEGER NOT NULL` in the Room-generated DDL and has no SQL
 * default, so an INSERT that omits it violates the constraint — and because the
 * statement is `INSERT OR IGNORE`, SQLite reports success while writing nothing.
 */
class AppInfoInsertTest {

    @Before
    fun loadDriver() {
        Class.forName("org.sqlite.JDBC")
    }

    /** The DDL Room generates for AppInfoEntity. */
    private fun createTable(conn: Connection) {
        conn.createStatement().use {
            it.execute(
                "CREATE TABLE IF NOT EXISTS `app_info` (`packageName` TEXT NOT NULL, " +
                    "`label` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                    "`alwaysAllowed` INTEGER NOT NULL, `limitMin` INTEGER, PRIMARY KEY(`packageName`))"
            )
        }
    }

    private fun count(conn: Connection): Int =
        conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM app_info").use { rs -> rs.next(); rs.getInt(1) }
        }

    @Test
    fun `omitting a NOT NULL column makes INSERT OR IGNORE write nothing`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            createTable(conn)
            // The pre-fix statement.
            conn.createStatement().use {
                it.execute(
                    "INSERT OR IGNORE INTO app_info (packageName, label, category) " +
                        "VALUES ('com.example.reader', 'Example Reader', 'Unclassified')"
                )
            }
            assertEquals("the old statement silently wrote no row", 0, count(conn))
        }
    }

    @Test
    fun `the shipped statement inserts the row with alwaysAllowed defaulted`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            createTable(conn)
            conn.createStatement().use {
                it.execute(
                    "INSERT OR IGNORE INTO app_info (packageName, label, category, alwaysAllowed, limitMin) " +
                        "VALUES ('com.example.reader', 'Example Reader', 'Unclassified', 0, NULL)"
                )
            }
            assertEquals(1, count(conn))
            conn.createStatement().use { st ->
                st.executeQuery("SELECT alwaysAllowed, limitMin, category FROM app_info").use { rs ->
                    rs.next()
                    assertEquals(0, rs.getInt(1))
                    assertEquals(null, rs.getObject(2))
                    assertEquals("Unclassified", rs.getString(3))
                }
            }
        }
    }

    @Test
    fun `a second insert for the same package is ignored, not duplicated`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            createTable(conn)
            val sql = "INSERT OR IGNORE INTO app_info (packageName, label, category, alwaysAllowed, limitMin) " +
                "VALUES (?, ?, 'Unclassified', 0, NULL)"
            repeat(2) { index ->
                conn.prepareStatement(sql).use { ps ->
                    ps.setString(1, "com.example.reader")
                    ps.setString(2, "Label $index")
                    ps.executeUpdate()
                }
            }
            assertEquals(1, count(conn))
            conn.createStatement().use { st ->
                st.executeQuery("SELECT label FROM app_info").use { rs ->
                    rs.next()
                    assertEquals("the first label wins", "Label 0", rs.getString(1))
                }
            }
        }
    }

    @Test
    fun `usage rows are unaffected by the app_info constraint`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            createTable(conn)
            conn.createStatement().use {
                it.execute(
                    "CREATE TABLE IF NOT EXISTS `app_usage` (`packageName` TEXT NOT NULL, `dateKey` TEXT NOT NULL, " +
                        "`foregroundMinutes` INTEGER NOT NULL, `pickups` INTEGER NOT NULL, " +
                        "`longestSessionMin` INTEGER NOT NULL, `source` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`packageName`, `dateKey`))"
                )
                it.execute(
                    "INSERT INTO app_usage VALUES ('com.example.reader', '2026-01-05', 45, 4, 25, 'device', 1767225600000)"
                )
            }
            assertEquals(0, count(conn))
            conn.createStatement().use { st ->
                st.executeQuery("SELECT COUNT(*) FROM app_usage").use { rs ->
                    rs.next()
                    assertEquals(1, rs.getInt(1))
                }
            }
        }
    }
}
