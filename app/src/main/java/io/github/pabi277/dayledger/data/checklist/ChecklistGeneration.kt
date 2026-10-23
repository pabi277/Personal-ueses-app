package io.github.pabi277.dayledger.data.checklist

import java.time.LocalDate

/**
 * Pure generation and ordering for the Phase 8 checklist.
 *
 * No Android types and no database handle, so every rule below is unit-tested on the JVM:
 * "a weekday habit and one-off task create only the correct occurrences", "generation is
 * idempotent", and "Now/Next ordering is honest about what is late".
 */

enum class Cadence { DAILY, WEEKDAYS, WEEKLY }

data class HabitSpec(
    val id: String,
    val title: String,
    val domain: String = "",
    val cadence: Cadence = Cadence.DAILY,
    /** ISO weekdays, 1 = Monday … 7 = Sunday. Only used by WEEKDAYS and WEEKLY. */
    val weekdays: Set<Int> = emptySet(),
    val windowStartMin: Int? = null,
    val windowEndMin: Int? = null,
    val minimumLabel: String? = null,
    val paused: Boolean = false,
    val archived: Boolean = false
)

data class TaskSpec(
    val id: String,
    val title: String,
    val domain: String = "",
    val dueDate: LocalDate,
    val dueTimeMin: Int? = null,
    val archived: Boolean = false
)

/** A generation result. The id is deterministic, which is the whole idempotency story. */
data class OccurrenceDraft(
    val id: String,
    val dateKey: String,
    val kind: String,
    val sourceId: String,
    val title: String,
    val domain: String,
    val windowStartMin: Int?,
    val windowEndMin: Int?,
    val minimumLabel: String?
)

object ChecklistGeneration {

    /**
     * `kind:source:date`. Stable across restarts, reboots and repeated generation, so the
     * same day can be generated any number of times and only ever produces one row.
     */
    fun occurrenceId(kind: String, sourceId: String, date: LocalDate): String =
        "$kind:$sourceId:$date"

    fun isoWeekday(date: LocalDate): Int = date.dayOfWeek.value // Monday = 1 … Sunday = 7

    fun isScheduledOn(habit: HabitSpec, date: LocalDate): Boolean {
        if (habit.archived || habit.paused) return false
        return when (habit.cadence) {
            Cadence.DAILY -> true
            Cadence.WEEKDAYS, Cadence.WEEKLY -> isoWeekday(date) in habit.weekdays
        }
    }

    fun parseWeekdays(csv: String): Set<Int> =
        csv.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()

    fun formatWeekdays(days: Set<Int>): String = days.sorted().joinToString(",")

    /**
     * Everything that should exist on [date]: scheduled habits plus one-off tasks due that
     * date. A paused or archived habit generates nothing; a task generates exactly one
     * occurrence on its due date and never recurs.
     */
    fun draftsFor(
        date: LocalDate,
        habits: List<HabitSpec>,
        tasks: List<TaskSpec>
    ): List<OccurrenceDraft> {
        val dateKey = date.toString()
        val fromHabits = habits
            .filter { isScheduledOn(it, date) }
            .map { habit ->
                OccurrenceDraft(
                    id = occurrenceId(ChecklistValues.KIND_HABIT, habit.id, date),
                    dateKey = dateKey,
                    kind = ChecklistValues.KIND_HABIT,
                    sourceId = habit.id,
                    title = habit.title,
                    domain = habit.domain,
                    windowStartMin = habit.windowStartMin,
                    windowEndMin = habit.windowEndMin,
                    minimumLabel = habit.minimumLabel
                )
            }
        val fromTasks = tasks
            .filter { !it.archived && it.dueDate == date }
            .map { task ->
                OccurrenceDraft(
                    id = occurrenceId(ChecklistValues.KIND_TASK, task.id, date),
                    dateKey = dateKey,
                    kind = ChecklistValues.KIND_TASK,
                    sourceId = task.id,
                    title = task.title,
                    domain = task.domain,
                    windowStartMin = task.dueTimeMin,
                    windowEndMin = task.dueTimeMin,
                    minimumLabel = null
                )
            }
        return (fromHabits + fromTasks).sortedWith(ordering)
    }

    private val ordering: Comparator<OccurrenceDraft> =
        compareBy<OccurrenceDraft> { it.windowStartMin ?: Int.MAX_VALUE }
            .thenBy { it.title.lowercase() }
}

/** Where a row belongs on the Today screen. */
enum class TodaySection { OVERDUE, NOW, NEXT, DUE_TODAY, ANYTIME, COMPLETED }

/** Minimal view of an occurrence the bucketing needs. */
data class TodayRow(
    val id: String,
    val title: String,
    val domain: String = "",
    val status: String,
    val windowStartMin: Int? = null,
    val windowEndMin: Int? = null,
    val snoozeUntil: Long? = null
)

data class TodayItem(val row: TodayRow, val section: TodaySection)

object TodayBuckets {

    /**
     * Now / Overdue / Next / Due today / Anytime / Completed, with no invented times:
     * an item without a window is *Anytime*, never given a made-up deadline.
     *
     * A snoozed item ("Later") stays pending and visible but leaves the Now/Next slots
     * until its snooze expires.
     */
    fun bucket(rows: List<TodayRow>, nowMinuteOfDay: Int, nowMillis: Long): List<TodayItem> {
        val open = rows.filterNot { ChecklistValues.isClosed(it.status) }
        val closed = rows.filter { ChecklistValues.isClosed(it.status) }
        val snoozed = open.filter { it.snoozeUntil != null && it.snoozeUntil > nowMillis }
        val live = open.filterNot { it in snoozed }

        val overdue = live.filter { it.windowEndMin != null && it.windowEndMin < nowMinuteOfDay }
        val now = live.filter {
            it.windowStartMin != null && it.windowEndMin != null &&
                it.windowStartMin <= nowMinuteOfDay && nowMinuteOfDay <= it.windowEndMin
        }
        val future = live
            .filter { it.windowStartMin != null && it.windowStartMin > nowMinuteOfDay }
            .sortedBy { it.windowStartMin }
        // Exactly one "next" — the soonest — so the screen answers "what now?" once.
        val next = future.take(1)
        val dueToday = future.drop(1)
        val anytime = (live.filter { it.windowStartMin == null } + snoozed)
            .sortedBy { it.title.lowercase() }

        return buildList {
            addAll(overdue.sortedBy { it.windowEndMin }.map { TodayItem(it, TodaySection.OVERDUE) })
            addAll(now.sortedBy { it.windowStartMin }.map { TodayItem(it, TodaySection.NOW) })
            addAll(next.map { TodayItem(it, TodaySection.NEXT) })
            addAll(dueToday.sortedBy { it.windowStartMin }.map { TodayItem(it, TodaySection.DUE_TODAY) })
            addAll(anytime.map { TodayItem(it, TodaySection.ANYTIME) })
            addAll(closed.sortedBy { it.title.lowercase() }.map { TodayItem(it, TodaySection.COMPLETED) })
        }
    }

    /** Core remaining = open items. Deliberately not capped at three. */
    fun remaining(rows: List<TodayRow>): Int = rows.count { !ChecklistValues.isClosed(it.status) }

    fun completedCounts(rows: List<TodayRow>): Pair<Int, Int> =
        rows.count { it.status == ChecklistValues.STATUS_FULL } to
            rows.count { it.status == ChecklistValues.STATUS_MINIMUM }
}
