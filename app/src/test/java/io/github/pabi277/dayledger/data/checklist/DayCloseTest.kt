package io.github.pabi277.dayledger.data.checklist

import io.github.pabi277.dayledger.data.migrations.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.DriverManager
import java.time.LocalDate

/**
 * Phase 9 "Done means", the parts provable without a device:
 *
 * - every unfinished item gets an explicit decision — nothing rolls silently;
 * - applying the same decision twice (a double tap, or a retried rollover) does not
 *   duplicate an occurrence;
 * - a repeated carryover is visible;
 * - Top 3 is at most three and never repeats;
 * - due order is honest and an undated item is never given an invented deadline.
 *
 * Every date, title and id here is fictional.
 */
class DayCloseTest {

    private val monday = LocalDate.of(2026, 10, 19)
    private val tuesday = monday.plusDays(1)

    @Before
    fun loadDriver() {
        Class.forName("org.sqlite.JDBC")
    }

    private fun row(
        id: String,
        title: String = "Fictional item",
        dateKey: String = monday.toString(),
        status: String = ChecklistValues.STATUS_PENDING
    ) = object : ChecklistOccurrenceish {
        override val id = id
        override val title = title
        override val dateKey = dateKey
        override val status = status
    }

    // ------------------------------------------------------------- leftovers

    @Test
    fun `open items are leftovers and settled ones are not`() {
        assertTrue(DayCloseRules.isLeftover(ChecklistValues.STATUS_PENDING))
        assertTrue(DayCloseRules.isLeftover(ChecklistValues.STATUS_LATER))
        listOf(
            ChecklistValues.STATUS_FULL,
            ChecklistValues.STATUS_MINIMUM,
            ChecklistValues.STATUS_SKIPPED,
            ChecklistValues.STATUS_CARRIED,
            ChecklistValues.STATUS_SCHEDULED,
            ChecklistValues.STATUS_DROPPED
        ).forEach { status ->
            assertFalse("$status is settled", DayCloseRules.isLeftover(status))
        }
    }

    @Test
    fun `each decision maps to its own status and action`() {
        assertEquals(ChecklistValues.STATUS_CARRIED, DayCloseRules.statusFor(LeftoverDecision.MOVE))
        assertEquals(ChecklistValues.STATUS_SCHEDULED, DayCloseRules.statusFor(LeftoverDecision.SCHEDULE))
        assertEquals(ChecklistValues.STATUS_MINIMUM, DayCloseRules.statusFor(LeftoverDecision.SHRINK_TO_MINIMUM))
        assertEquals(ChecklistValues.STATUS_DROPPED, DayCloseRules.statusFor(LeftoverDecision.DROP))

        assertEquals(ChecklistValues.ACTION_MOVE, DayCloseRules.actionFor(LeftoverDecision.MOVE))
        assertEquals(ChecklistValues.ACTION_SCHEDULE, DayCloseRules.actionFor(LeftoverDecision.SCHEDULE))
        assertEquals(ChecklistValues.ACTION_MINIMUM, DayCloseRules.actionFor(LeftoverDecision.SHRINK_TO_MINIMUM))
        assertEquals(ChecklistValues.ACTION_DROP, DayCloseRules.actionFor(LeftoverDecision.DROP))
    }

    @Test
    fun `only move and schedule carry an item to another date`() {
        assertTrue(DayCloseRules.carriesForward(LeftoverDecision.MOVE))
        assertTrue(DayCloseRules.carriesForward(LeftoverDecision.SCHEDULE))
        assertFalse(DayCloseRules.carriesForward(LeftoverDecision.SHRINK_TO_MINIMUM))
        assertFalse(DayCloseRules.carriesForward(LeftoverDecision.DROP))
    }

    @Test
    fun `a carried id is stable, dated, and never collides with a habit id`() {
        val carried = DayCloseRules.carriedId("read", tuesday)
        assertEquals("carried:read:$tuesday", carried)
        assertEquals(carried, DayCloseRules.carriedId("read", tuesday))
        assertNotEquals(
            ChecklistGeneration.occurrenceId(ChecklistValues.KIND_HABIT, "read", tuesday),
            carried
        )
    }

    @Test
    fun `occurrence ids parse back to their source`() {
        assertEquals("habit" to "read", DayCloseRules.parseOccurrenceId("habit:read:2026-10-19"))
        assertEquals("task" to "t1", DayCloseRules.parseOccurrenceId("task:t1:2026-10-19"))
        assertEquals("carried" to "read", DayCloseRules.parseOccurrenceId("carried:read:2026-10-20"))
        assertNull(DayCloseRules.parseOccurrenceId("nonsense"))
        assertNull(DayCloseRules.parseOccurrenceId("other:read:2026-10-19"))
        assertEquals("read", DayCloseRules.sourceIdOf("habit:read:2026-10-19"))
    }

    @Test
    fun `carryover is counted from the log and flagged from the second carry`() {
        val logs = listOf(
            CarryoverLogRow("habit:read:2026-10-17", ChecklistValues.ACTION_MOVE),
            CarryoverLogRow("habit:read:2026-10-18", ChecklistValues.ACTION_SCHEDULE),
            CarryoverLogRow("habit:walk:2026-10-18", ChecklistValues.ACTION_MOVE)
        )
        assertEquals(2, DayCloseRules.carryoverCount(logs, "read"))
        assertEquals(1, DayCloseRules.carryoverCount(logs, "walk"))
        assertEquals(0, DayCloseRules.carryoverCount(logs, "unknown"))

        assertFalse(DayCloseRules.repeatedCarryover(0))
        assertFalse(DayCloseRules.repeatedCarryover(1))
        assertTrue(DayCloseRules.repeatedCarryover(2))
    }

    @Test
    fun `leftovers include earlier open days and exclude settled and future rows`() {
        val leftovers = DayCloseRules.leftoversFor(
            listOf(
                row("habit:a:$monday", title = "Zeta", dateKey = monday.toString()),
                row("habit:b:$monday", title = "Alpha", dateKey = monday.toString()),
                row(
                    "habit:c:$monday",
                    title = "Already done",
                    dateKey = monday.toString(),
                    status = ChecklistValues.STATUS_FULL
                ),
                row("habit:d:$tuesday", title = "Tomorrow's item", dateKey = tuesday.toString()),
                row("nonsense", title = "Ignored", dateKey = monday.toString())
            ),
            throughDate = monday
        )
        assertEquals(listOf("Alpha", "Zeta"), leftovers.map { it.title })
        assertTrue(leftovers.all { it.kind == "habit" })
    }

    @Test
    fun `carryover counts attach to the matching source only`() {
        val leftovers = DayCloseRules.leftoversFor(
            listOf(
                row("habit:read:$monday", title = "Read", dateKey = monday.toString()),
                row("habit:walk:$monday", title = "Walk", dateKey = monday.toString())
            ),
            throughDate = monday
        )
        val logs = listOf(
            CarryoverLogRow("habit:read:2026-10-17", ChecklistValues.ACTION_MOVE),
            CarryoverLogRow("habit:read:2026-10-18", ChecklistValues.ACTION_MOVE)
        )
        val counted = DayCloseRules.withCarryoverCounts(leftovers, logs)
        assertEquals(2, counted.first { it.sourceId == "read" }.carriedBefore)
        assertTrue(counted.first { it.sourceId == "read" }.repeated)
        assertFalse(counted.first { it.sourceId == "walk" }.repeated)
    }

    // ------------------------------------------------------------- Top 3

    @Test
    fun `top 3 keeps at most three and refuses repeats`() {
        var list = emptyList<String>()
        list = DayCloseRules.top3Add(list, "a")
        list = DayCloseRules.top3Add(list, "b")
        list = DayCloseRules.top3Add(list, "c")
        assertEquals(3, list.size)
        // A fourth is refused rather than silently evicting something the owner chose.
        assertEquals(list, DayCloseRules.top3Add(list, "d"))
        // Re-adding an existing id changes nothing.
        assertEquals(list, DayCloseRules.top3Add(list, "a"))
        assertEquals(listOf("a", "c"), DayCloseRules.top3Remove(list, "b"))
    }

    @Test
    fun `top 3 survives a round trip through settings text`() {
        val encoded = DayCloseRules.top3Encode(listOf("habit:x:2026-10-19", "task:t1:2026-10-19"))
        assertEquals(
            listOf("habit:x:2026-10-19", "task:t1:2026-10-19"),
            DayCloseRules.top3Parse(encoded)
        )
        assertEquals(emptyList<String>(), DayCloseRules.top3Parse(null))
        assertEquals(emptyList<String>(), DayCloseRules.top3Parse(""))
        // Duplicates in stored text collapse, and more than three truncates.
        assertEquals(listOf("a", "b"), DayCloseRules.top3Parse("a,b,a"))
        assertEquals(3, DayCloseRules.top3Parse("a,b,c,d").size)
        assertEquals("top3.2026-10-19", DayCloseRules.top3Key("2026-10-19"))
    }

    // ----------------------------------------------------------- deadlines

    @Test
    fun `due order is by date, then time, then title, with undated items last`() {
        data class Due(val title: String, val date: LocalDate?, val time: Int?)
        val items = listOf(
            Due("No date", null, null),
            Due("Later today", monday, 18 * 60),
            Due("Tomorrow", tuesday, null),
            Due("Morning", monday, 9 * 60),
            Due("Another morning", monday, 9 * 60)
        )
        val sorted = items.sortedWith(
            DayCloseRules.urgencyComparator({ it.date }, { it.time }, { it.title })
        )
        assertEquals(
            listOf("Another morning", "Morning", "Later today", "Tomorrow", "No date"),
            sorted.map { it.title }
        )
    }

    @Test
    fun `due labels are honest and never invent a time`() {
        assertEquals("Today", DayCloseRules.dueLabel(monday, monday))
        assertEquals("Tomorrow", DayCloseRules.dueLabel(tuesday, monday))
        assertEquals("in 3 days", DayCloseRules.dueLabel(monday.plusDays(3), monday))
        assertEquals("Overdue by 1 day", DayCloseRules.dueLabel(monday.minusDays(1), monday))
        assertEquals("Overdue by 2 days", DayCloseRules.dueLabel(monday.minusDays(2), monday))
        assertEquals("Today 09:30", DayCloseRules.dueLabel(monday, monday, 9 * 60 + 30))
    }

    // ------------------------------------- idempotency of a carry, in real SQL

    @Test
    fun `applying the same move twice leaves exactly one carried occurrence`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            // Real v6 and v7 statements: the occurrences table and the inbox table.
            DatabaseMigrations.statementsFrom(5).forEach { sql ->
                conn.createStatement().use { it.execute(sql) }
            }
            val carriedId = DayCloseRules.carriedId("read", tuesday)
            val insert =
                "INSERT OR IGNORE INTO checklist_occurrences " +
                    "(id, dateKey, kind, sourceId, title, domain, status, generatedAt, updatedAt) " +
                    "VALUES (?, ?, 'carried', ?, 'Fictional read', '', 'pending', 0, 0)"
            repeat(2) {
                conn.prepareStatement(insert).use { statement ->
                    statement.setString(1, carriedId)
                    statement.setString(2, tuesday.toString())
                    statement.setString(3, "read")
                    statement.executeUpdate()
                }
            }
            conn.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT COUNT(*) FROM checklist_occurrences WHERE kind = 'carried'"
                ).use { rs ->
                    rs.next()
                    assertEquals("a double tap must not duplicate the carried item", 1, rs.getInt(1))
                }
            }
        }
    }

    @Test
    fun `a carried id stays unique per target date`() {
        val wednesday = tuesday.plusDays(1)
        assertNotEquals(
            DayCloseRules.carriedId("read", tuesday),
            DayCloseRules.carriedId("read", wednesday)
        )
    }
}
