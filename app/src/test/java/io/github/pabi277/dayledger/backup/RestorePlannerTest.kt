package io.github.pabi277.dayledger.backup

import io.github.pabi277.dayledger.data.migrations.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Restore safety: what the app accepts, what it refuses, and the promise that a
 * refusal happens *before* a single row is deleted.
 */
class RestorePlannerTest {

    private val supported = DatabaseMigrations.SUPPORTED_VERSIONS

    private val currentTables = listOf(
        TableSpec(
            "categories",
            listOf(
                ColumnSpec("id", notNull = true, hasDefault = false),
                ColumnSpec("name", notNull = true, hasDefault = false),
                ColumnSpec("kind", notNull = true, hasDefault = false),
                ColumnSpec("budgetMin", notNull = true, hasDefault = false),
                ColumnSpec("color", notNull = true, hasDefault = false),
                // Added by a hypothetical later schema, with a default.
                ColumnSpec("archived", notNull = true, hasDefault = true)
            )
        ),
        TableSpec(
            "app_info",
            listOf(
                ColumnSpec("packageName", notNull = true, hasDefault = false),
                // Added later WITHOUT a default: an old backup cannot fill it.
                ColumnSpec("firstSeen", notNull = true, hasDefault = false)
            )
        )
    )

    private fun document(schemaVersion: Int, tables: List<BackupTable>) = BackupDocument(
        formatVersion = BackupCodec.FORMAT_VERSION,
        schemaVersion = schemaVersion,
        appId = BackupCodec.APP_ID,
        appVersionName = "0.1.0",
        createdAtEpochMs = 1L,
        tables = tables
    )

    @Test
    fun `a current backup is accepted and reports its row counts`() {
        val doc = document(
            5,
            listOf(
                BackupTable("categories", listOf("id", "name", "kind", "budgetMin", "color"),
                    listOf(listOf(BackupCell.Text("study"), BackupCell.Text("Study"), BackupCell.Text("floor"),
                        BackupCell.Integer(90), BackupCell.Integer(1)))),
                BackupTable("app_info", listOf("packageName", "firstSeen"),
                    emptyList())
            )
        )
        val decision = RestorePlanner.plan(doc, currentTables, 5, supported)
        assertTrue(decision is RestoreDecision.Allowed)
        val allowed = decision as RestoreDecision.Allowed
        assertEquals(listOf("categories", "app_info"), allowed.tables.map { it.table })
        assertEquals(1, allowed.tables.first().rowCount)
        assertEquals(emptyList<String>(), allowed.untouched)
    }

    @Test
    fun `a backup from a newer schema is refused`() {
        val decision = RestorePlanner.plan(
            document(6, listOf(BackupTable("categories", listOf("id"), emptyList()))),
            currentTables, 5, supported
        )
        assertTrue(decision is RestoreDecision.Rejected)
        assertTrue((decision as RestoreDecision.Rejected).reasons.first().contains("newer database schema"))
    }

    @Test
    fun `an unsupported older schema is refused`() {
        val decision = RestorePlanner.plan(
            document(0, listOf(BackupTable("categories", listOf("id"), emptyList()))),
            currentTables, 5, listOf(1, 2, 3, 4, 5)
        )
        assertTrue(decision is RestoreDecision.Rejected)
    }

    @Test
    fun `an older backup that fits the current table is accepted`() {
        val decision = RestorePlanner.plan(
            document(1, listOf(
                BackupTable("categories", listOf("id", "name", "kind", "budgetMin", "color"),
                    listOf(listOf(BackupCell.Text("study"), BackupCell.Text("Study"), BackupCell.Text("floor"),
                        BackupCell.Integer(90), BackupCell.Integer(1))))
            )),
            currentTables, 5, supported
        )
        assertTrue(decision is RestoreDecision.Allowed)
    }

    @Test
    fun `an older backup missing a required column is refused`() {
        val decision = RestorePlanner.plan(
            document(1, listOf(BackupTable("app_info", listOf("packageName"),
                listOf(listOf(BackupCell.Text("com.example.reader")))))),
            currentTables, 5, supported
        )
        assertTrue(decision is RestoreDecision.Rejected)
        val reasons = (decision as RestoreDecision.Rejected).reasons
        assertTrue(reasons.any { it.contains("firstSeen") })
    }

    @Test
    fun `an unknown table is refused rather than skipped`() {
        val decision = RestorePlanner.plan(
            document(5, listOf(BackupTable("checklist_items", listOf("id"), emptyList()))),
            currentTables, 5, supported
        )
        assertTrue(decision is RestoreDecision.Rejected)
        assertTrue((decision as RestoreDecision.Rejected).reasons.first().contains("does not have"))
    }

    @Test
    fun `an unknown column is refused`() {
        val decision = RestorePlanner.plan(
            document(5, listOf(BackupTable("categories", listOf("id", "notAColumn"), emptyList()))),
            currentTables, 5, supported
        )
        assertTrue(decision is RestoreDecision.Rejected)
    }

    @Test
    fun `tables absent from the backup are reported as untouched`() {
        val doc = document(5, listOf(BackupTable("categories", listOf("id", "name", "kind", "budgetMin", "color"), emptyList())))
        val decision = RestorePlanner.plan(doc, currentTables, 5, supported)
        val allowed = decision as RestoreDecision.Allowed
        assertEquals(listOf("app_info"), allowed.untouched)
        assertEquals(listOf("app_info"), RestorePlanner.untouchedTables(doc, currentTables))
    }

    @Test
    fun `every rejection carries at least one readable reason`() {
        val rejectedCases = listOf(
            document(99, listOf(BackupTable("categories", listOf("id"), emptyList()))),
            document(5, listOf(BackupTable("nope", listOf("id"), emptyList()))),
            document(5, listOf(BackupTable("categories", listOf("id", "bogus"), emptyList())))
        )
        rejectedCases.forEach { doc ->
            val decision = RestorePlanner.plan(doc, currentTables, 5, supported)
            assertTrue(decision is RestoreDecision.Rejected)
            val reasons = (decision as RestoreDecision.Rejected).reasons
            assertTrue(reasons.isNotEmpty())
            assertTrue(reasons.all { it.isNotBlank() && it.length > 10 })
        }
    }
}
