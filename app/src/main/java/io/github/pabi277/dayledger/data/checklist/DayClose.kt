package io.github.pabi277.dayledger.data.checklist

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Phase 9 — capture, deadlines and carryover. Pure rules only, so the clauses that matter
 * are unit-tested on the JVM with fictional dates and titles:
 *
 * - every unfinished item gets an explicit decision, and nothing rolls silently;
 * - a repeated carryover is visible rather than hidden;
 * - the merged Tomorrow list (template blocks, due tasks, carryovers) cannot duplicate;
 * - Top 3 is at most three, with no repeats;
 * - due urgency is honest, and an undated item is never given an invented deadline.
 */
enum class LeftoverDecision {
    /** Push the item to another date. */
    MOVE,

    /** Give it a chosen date deliberately (same mechanics, different intent). */
    SCHEDULE,

    /** Do the small version now and settle the item. */
    SHRINK_TO_MINIMUM,

    /** Drop it on purpose. Recorded, never silent. */
    DROP
}

data class Leftover(
    val id: String,
    val title: String,
    val kind: String,
    val sourceId: String,
    val dateKey: String,
    val status: String,
    /** How many times this same source has already been moved on. */
    val carriedBefore: Int = 0
) {
    val repeated: Boolean get() = DayCloseRules.repeatedCarryover(carriedBefore)
}

/** The parts of a completion log the carryover counter needs. */
data class CarryoverLogRow(val occurrenceId: String, val action: String)

object DayCloseRules {

    const val KIND_CARRIED = "carried"

    /** An item is a leftover while it is still open — pending or snoozed. */
    fun isLeftover(status: String): Boolean = !ChecklistValues.isClosed(status)

    fun statusFor(decision: LeftoverDecision): String = when (decision) {
        LeftoverDecision.MOVE -> ChecklistValues.STATUS_CARRIED
        LeftoverDecision.SCHEDULE -> ChecklistValues.STATUS_SCHEDULED
        LeftoverDecision.SHRINK_TO_MINIMUM -> ChecklistValues.STATUS_MINIMUM
        LeftoverDecision.DROP -> ChecklistValues.STATUS_DROPPED
    }

    fun actionFor(decision: LeftoverDecision): String = when (decision) {
        LeftoverDecision.MOVE -> ChecklistValues.ACTION_MOVE
        LeftoverDecision.SCHEDULE -> ChecklistValues.ACTION_SCHEDULE
        LeftoverDecision.SHRINK_TO_MINIMUM -> ChecklistValues.ACTION_MINIMUM
        LeftoverDecision.DROP -> ChecklistValues.ACTION_DROP
    }

    /** Whether a decision produces an occurrence on another date. */
    fun carriesForward(decision: LeftoverDecision): Boolean =
        decision == LeftoverDecision.MOVE || decision == LeftoverDecision.SCHEDULE

    /**
     * Deterministic id for a carried occurrence: `carried:<sourceId>:<date>`.
     *
     * A distinct `carried` kind means it can never collide with the habit-generated
     * `habit:<sourceId>:<date>` row for the same day, and applying the same decision twice
     * targets the same primary key — which is what stops a rollover from duplicating.
     */
    fun carriedId(sourceId: String, targetDate: LocalDate): String =
        "$KIND_CARRIED:$sourceId:$targetDate"

    /** `habit:read:2026-10-23` → `habit` to `read`. */
    fun parseOccurrenceId(id: String): Pair<String, String>? {
        val parts = id.split(':')
        if (parts.size < 3) return null
        val kind = parts.first()
        if (kind != ChecklistValues.KIND_HABIT && kind != ChecklistValues.KIND_TASK && kind != KIND_CARRIED) {
            return null
        }
        // The source id never contains ':' by construction; the date is the last segment.
        val sourceId = parts.subList(1, parts.size - 1).joinToString(":")
        return kind to sourceId
    }

    fun sourceIdOf(occurrenceId: String): String? = parseOccurrenceId(occurrenceId)?.second

    /**
     * How many times this source has already been carried. Counted from the append-only
     * log rather than stored on the row, so undoing a decision cannot corrupt the count.
     */
    fun carryoverCount(logs: List<CarryoverLogRow>, sourceId: String): Int =
        logs.count { DayCloseRules.sourceIdOf(it.occurrenceId) == sourceId }

    /** Shame-free but visible: flagged from the second carry onward. */
    fun repeatedCarryover(carriedBefore: Int): Boolean = carriedBefore >= 2

    /**
     * Which occurrences a day close must ask about.
     *
     * `throughDate` exists so a missed evening does not bury yesterday's leftovers: the
     * close screen lists anything still open on or before the day being closed.
     */
    fun leftoversFor(
        rows: List<ChecklistOccurrenceish>,
        throughDate: LocalDate
    ): List<Leftover> =
        rows.asSequence()
            .filter { isLeftover(it.status) }
            .filter { runCatching { LocalDate.parse(it.dateKey) <= throughDate }.getOrDefault(false) }
            .sortedWith(compareBy<ChecklistOccurrenceish>({ it.dateKey }, { it.title.lowercase() }))
            .mapNotNull { row ->
                val parsed = parseOccurrenceId(row.id) ?: return@mapNotNull null
                Leftover(
                    id = row.id,
                    title = row.title,
                    kind = parsed.first,
                    sourceId = parsed.second,
                    dateKey = row.dateKey,
                    status = row.status
                )
            }
            .toList()

    /**
     * Attach carryover counts to the leftovers. Separate from [leftoversFor] so the counting
     * stays a pure list operation.
     */
    fun withCarryoverCounts(leftovers: List<Leftover>, logs: List<CarryoverLogRow>): List<Leftover> =
        leftovers.map { it.copy(carriedBefore = carryoverCount(logs, it.sourceId)) }

    // ---------------------------------------------------------------- Top 3

    /**
     * Top 3 is a planning aid, not a hard limit on the day (UI Design v2 §3). Stored as a
     * CSV of occurrence ids under one `settings` key per date.
     */
    fun top3Parse(csv: String?): List<String> =
        csv.orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(3)

    fun top3Encode(ids: List<String>): String = top3Parse(ids.joinToString(",")).joinToString(",")

    /** Returns the new list; adding a fourth replaces nothing and is refused by the caller. */
    fun top3Add(current: List<String>, id: String): List<String> {
        val base = top3Parse(current.joinToString(","))
        if (id in base) return base
        if (base.size >= 3) return base
        return base + id
    }

    fun top3Remove(current: List<String>, id: String): List<String> =
        top3Parse(current.joinToString(",")).filterNot { it == id }

    fun top3Key(dateKey: String): String = "top3.$dateKey"

    // ------------------------------------------------------------- deadlines

    /**
     * Urgency order for due work: earliest date first, then time, then title. An item with
     * no date is *not* urgent by default and sorts last.
     */
    fun <T> urgencyComparator(
        dateOf: (T) -> LocalDate?,
        timeOf: (T) -> Int?,
        titleOf: (T) -> String
    ): Comparator<T> = Comparator { a, b ->
        val da = dateOf(a)
        val db = dateOf(b)
        when {
            da == null && db == null -> 0
            da == null -> 1
            db == null -> -1
            else -> {
                val byDate = da.compareTo(db)
                if (byDate != 0) byDate else {
                    val byTime = (timeOf(a) ?: Int.MAX_VALUE).compareTo(timeOf(b) ?: Int.MAX_VALUE)
                    if (byTime != 0) byTime else titleOf(a).lowercase().compareTo(titleOf(b).lowercase())
                }
            }
        }
    }

    /** "Overdue by 2 days", "Today", "Tomorrow", "in 3 days" — never a fake time. */
    fun dueLabel(dueDate: LocalDate, today: LocalDate, dueTimeMin: Int? = null): String {
        val days = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val time = dueTimeMin?.let { " %02d:%02d".format(it / 60, it % 60) } ?: ""
        return when {
            days < 0 -> "Overdue by ${-days} day${if (days == -1) "" else "s"}$time"
            days == 0 -> "Today$time"
            days == 1 -> "Tomorrow$time"
            else -> "in $days days$time"
        }
    }
}

/** Minimal shape [DayCloseRules] needs, so tests never touch Room types. */
interface ChecklistOccurrenceish {
    val id: String
    val title: String
    val dateKey: String
    val status: String
}
