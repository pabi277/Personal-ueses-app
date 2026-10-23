package io.github.pabi277.dayledger.data.checklist

import io.github.pabi277.dayledger.data.migrations.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.DriverManager
import java.time.LocalDate

/**
 * Phase 8 "Done means", the parts that can be proven without a device:
 *
 * - a weekday habit and a one-off task create only the correct occurrences;
 * - generation is idempotent (restart/reboot adds no duplicates);
 * - Now/Next ordering is honest, and Anytime never invents a time.
 *
 * All dates, titles and ids are fictional.
 */
class ChecklistGenerationTest {

    // 2026-10-19 is a Monday; the whole week below is fictional test data.
    private val monday = LocalDate.of(2026, 10, 19)
    private val tuesday = monday.plusDays(1)
    private val wednesday = monday.plusDays(2)
    private val sunday = monday.plusDays(6)

    @Before
    fun loadDriver() {
        Class.forName("org.sqlite.JDBC")
    }

    private fun habit(
        id: String = "h1",
        title: String = "Read one page",
        cadence: Cadence = Cadence.DAILY,
        weekdays: Set<Int> = emptySet(),
        paused: Boolean = false,
        archived: Boolean = false,
        start: Int? = null,
        end: Int? = null
    ) = HabitSpec(id, title, cadence = cadence, weekdays = weekdays,
        windowStartMin = start, windowEndMin = end, paused = paused, archived = archived)

    // ------------------------------------------------------------- generation

    @Test
    fun `a weekday habit generates only on its chosen weekdays`() {
        // Monday, Wednesday and Friday only.
        val weekly = habit(cadence = Cadence.WEEKDAYS, weekdays = setOf(1, 3, 5))
        assertTrue(ChecklistGeneration.isScheduledOn(weekly, monday))
        assertTrue(!ChecklistGeneration.isScheduledOn(weekly, tuesday))
        assertTrue(ChecklistGeneration.isScheduledOn(weekly, wednesday))
        assertTrue(!ChecklistGeneration.isScheduledOn(weekly, sunday))
    }

    @Test
    fun `a weekly habit generates only on its one day`() {
        val weekly = habit(cadence = Cadence.WEEKLY, weekdays = setOf(3))
        assertEquals(3, ChecklistGeneration.isoWeekday(wednesday))
        assertTrue(ChecklistGeneration.isScheduledOn(weekly, wednesday))
        assertTrue(!ChecklistGeneration.isScheduledOn(weekly, monday))
    }

    @Test
    fun `a daily habit generates every day`() {
        val daily = habit()
        (0..6).forEach { offset ->
            assertTrue(ChecklistGeneration.isScheduledOn(daily, monday.plusDays(offset.toLong())))
        }
    }

    @Test
    fun `a paused or archived habit generates nothing further`() {
        assertTrue(!ChecklistGeneration.isScheduledOn(habit(paused = true), monday))
        assertTrue(!ChecklistGeneration.isScheduledOn(habit(archived = true), monday))
    }

    @Test
    fun `a one-off task creates exactly one occurrence, on its due date`() {
        val task = TaskSpec(id = "t1", title = "Return the library book", dueDate = tuesday)
        val week = (0..6).map { ChecklistGeneration.draftsFor(monday.plusDays(it.toLong()), emptyList(), listOf(task)) }
        assertEquals(1, week.sumOf { it.size })
        val only = week.first { it.isNotEmpty() }.single()
        assertEquals("task:t1:$tuesday", only.id)
        assertEquals(tuesday.toString(), only.dateKey)
    }

    @Test
    fun `an archived task generates nothing`() {
        val task = TaskSpec(id = "t1", title = "Gone", dueDate = monday, archived = true)
        assertTrue(ChecklistGeneration.draftsFor(monday, emptyList(), listOf(task)).isEmpty())
    }

    @Test
    fun `the generated id is stable for the same source and date`() {
        // This is the whole idempotency guarantee: same inputs, same primary key.
        val first = ChecklistGeneration.draftsFor(monday, listOf(habit()), emptyList())
        val second = ChecklistGeneration.draftsFor(monday, listOf(habit()), emptyList())
        assertEquals(first.map { it.id }, second.map { it.id })
        assertEquals("habit:h1:$monday", first.single().id)
    }

    @Test
    fun `different dates produce different ids for the same habit`() {
        val a = ChecklistGeneration.draftsFor(monday, listOf(habit()), emptyList()).single()
        val b = ChecklistGeneration.draftsFor(tuesday, listOf(habit()), emptyList()).single()
        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `drafts keep the habit snapshot and are ordered by window then title`() {
        val habits = listOf(
            habit(id = "late", title = "Evening walk", start = 20 * 60, end = 21 * 60),
            habit(id = "early", title = "Morning pages", start = 7 * 60, end = 7 * 60 + 30),
            habit(id = "any", title = "Anytime stretch")
        )
        val drafts = ChecklistGeneration.draftsFor(monday, habits, emptyList())
        assertEquals(listOf("Morning pages", "Evening walk", "Anytime stretch"), drafts.map { it.title })
        assertEquals(7 * 60, drafts.first().windowStartMin)
    }

    @Test
    fun `weekday parsing ignores junk and out-of-range values`() {
        assertEquals(setOf(1, 3, 5), ChecklistGeneration.parseWeekdays("1,3,5"))
        assertEquals(setOf(1, 3, 7), ChecklistGeneration.parseWeekdays(" 1, 3 ,9,0,x,7"))
        assertEquals("1,3,5", ChecklistGeneration.formatWeekdays(setOf(5, 1, 3)))
    }

    // -------------------------------------------------- idempotency, in SQL

    @Test
    fun `inserting the same occurrence id twice leaves exactly one row`() {
        // Runs the real v5 → v6 statements against real SQLite, then inserts the same
        // generated id twice the way the app does (INSERT OR IGNORE).
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            DatabaseMigrations.statementsFrom(5).forEach { sql ->
                conn.createStatement().use { it.execute(sql) }
            }
            val id = ChecklistGeneration.draftsFor(
                monday, listOf(habit()), emptyList()
            ).single().id
            val insert =
                "INSERT OR IGNORE INTO checklist_occurrences " +
                    "(id, dateKey, kind, sourceId, title, domain, status, generatedAt, updatedAt) " +
                    "VALUES (?, ?, ?, ?, ?, '', 'pending', 0, 0)"
            repeat(3) {
                conn.prepareStatement(insert).use { statement ->
                    statement.setString(1, id)
                    statement.setString(2, monday.toString())
                    statement.setString(3, ChecklistValues.KIND_HABIT)
                    statement.setString(4, "h1")
                    statement.setString(5, "Read one page")
                    statement.executeUpdate()
                }
            }
            conn.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM checklist_occurrences").use { rs ->
                    rs.next()
                    assertEquals("three generations of one date must leave one row", 1, rs.getInt(1))
                }
            }
        }
    }

    @Test
    fun `a weekday habit regenerated across a week creates exactly the scheduled days`() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { conn ->
            DatabaseMigrations.statementsFrom(5).forEach { sql ->
                conn.createStatement().use { it.execute(sql) }
            }
            val habit = habit(cadence = Cadence.WEEKDAYS, weekdays = setOf(1, 3, 5))
            val insert =
                "INSERT OR IGNORE INTO checklist_occurrences " +
                    "(id, dateKey, kind, sourceId, title, domain, status, generatedAt, updatedAt) " +
                    "VALUES (?, ?, ?, ?, ?, '', 'pending', 0, 0)"
            // Generate the week twice, exactly as a restart would.
            repeat(2) {
                (0..6).forEach { offset ->
                    val date = monday.plusDays(offset.toLong())
                    ChecklistGeneration.draftsFor(date, listOf(habit), emptyList()).forEach { draft ->
                        conn.prepareStatement(insert).use { statement ->
                            statement.setString(1, draft.id)
                            statement.setString(2, draft.dateKey)
                            statement.setString(3, draft.kind)
                            statement.setString(4, draft.sourceId)
                            statement.setString(5, draft.title)
                            statement.executeUpdate()
                        }
                    }
                }
            }
            conn.createStatement().use { statement ->
                statement.executeQuery("SELECT dateKey FROM checklist_occurrences ORDER BY dateKey").use { rs ->
                    val dates = ArrayList<String>()
                    while (rs.next()) dates.add(rs.getString(1))
                    assertEquals(
                        listOf(monday.toString(), wednesday.toString(), monday.plusDays(4).toString()),
                        dates
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------- bucketing

    private fun row(
        id: String,
        status: String = ChecklistValues.STATUS_PENDING,
        start: Int? = null,
        end: Int? = null,
        snooze: Long? = null,
        title: String = id
    ) = TodayRow(id, title, status = status, windowStartMin = start, windowEndMin = end, snoozeUntil = snooze)

    private fun sectionOf(items: List<TodayItem>, id: String) =
        items.first { it.row.id == id }.section

    @Test
    fun `a timed item in its window is Now and a passed item is Overdue`() {
        val now = 10 * 60
        val items = TodayBuckets.bucket(
            listOf(
                row("inside", start = 9 * 60 + 30, end = 10 * 60 + 30),
                row("passed", start = 8 * 60, end = 9 * 60)
            ),
            now, nowMillis = 0
        )
        assertEquals(TodaySection.NOW, sectionOf(items, "inside"))
        assertEquals(TodaySection.OVERDUE, sectionOf(items, "passed"))
    }

    @Test
    fun `exactly one item is Next and the rest are Due today`() {
        val now = 10 * 60
        val items = TodayBuckets.bucket(
            listOf(
                row("later1", start = 12 * 60, end = 12 * 60 + 30),
                row("sooner", start = 11 * 60, end = 11 * 60 + 30),
                row("latest", start = 18 * 60, end = 18 * 60 + 30)
            ),
            now, nowMillis = 0
        )
        assertEquals(TodaySection.NEXT, sectionOf(items, "sooner"))
        assertEquals(TodaySection.DUE_TODAY, sectionOf(items, "later1"))
        assertEquals(TodaySection.DUE_TODAY, sectionOf(items, "latest"))
        assertEquals(1, items.count { it.section == TodaySection.NEXT })
    }

    @Test
    fun `an untimed item is Anytime and is never given a window`() {
        val items = TodayBuckets.bucket(listOf(row("anytime")), 10 * 60, nowMillis = 0)
        assertEquals(TodaySection.ANYTIME, sectionOf(items, "anytime"))
        assertEquals(null, items.single().row.windowStartMin)
    }

    @Test
    fun `a snoozed item leaves Now and returns to Anytime until the snooze expires`() {
        val now = 10 * 60
        val snoozed = row("snoozed", start = 9 * 60, end = 11 * 60, snooze = 5_000)
        val items = TodayBuckets.bucket(listOf(snoozed), now, nowMillis = 1_000)
        assertEquals(TodaySection.ANYTIME, sectionOf(items, "snoozed"))

        val expired = TodayBuckets.bucket(listOf(snoozed), now, nowMillis = 9_000)
        assertEquals(TodaySection.NOW, sectionOf(expired, "snoozed"))
    }

    @Test
    fun `settled items collapse into Completed and the counters separate Full and Minimum`() {
        val rows = listOf(
            row("a", status = ChecklistValues.STATUS_FULL),
            row("b", status = ChecklistValues.STATUS_MINIMUM),
            row("c", status = ChecklistValues.STATUS_SKIPPED),
            row("d")
        )
        val items = TodayBuckets.bucket(rows, 10 * 60, nowMillis = 0)
        assertEquals(TodaySection.COMPLETED, sectionOf(items, "a"))
        assertEquals(TodaySection.COMPLETED, sectionOf(items, "b"))
        assertEquals(TodaySection.COMPLETED, sectionOf(items, "c"))
        assertEquals(1, TodayBuckets.remaining(rows))
        assertEquals(1 to 1, TodayBuckets.completedCounts(rows))
    }

    @Test
    fun `skipped is settled but is not counted as a completion`() {
        assertTrue(ChecklistValues.isClosed(ChecklistValues.STATUS_SKIPPED))
        assertTrue(!ChecklistValues.isCompleted(ChecklistValues.STATUS_SKIPPED))
        assertTrue(ChecklistValues.isCompleted(ChecklistValues.STATUS_MINIMUM))
    }

    @Test
    fun `remaining is not capped at three`() {
        val rows = (1..6).map { row("r$it") }
        assertEquals(6, TodayBuckets.remaining(rows))
    }
}
