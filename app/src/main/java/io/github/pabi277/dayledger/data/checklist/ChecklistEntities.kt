package io.github.pabi277.dayledger.data.checklist

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Phase 8 — checklist core, schema v6.
 *
 * Four concepts, kept apart exactly as `docs/REQUIREMENTS.md` defines them:
 *
 * - [HabitRuleEntity] — a reusable recurrence definition. Editing it must never rewrite
 *   earlier records, which is why occurrences snapshot their title and window.
 * - [ChecklistTaskEntity] — a single intent with a date-only (optionally timed) deadline.
 *   It does not recur.
 * - [ChecklistOccurrenceEntity] — the dated instance of a habit or task. Its primary key
 *   is derived from (kind, source, date), which is what makes generation idempotent: a
 *   restart, a reboot or a second run regenerates the *same* ids and adds no duplicates.
 * - [CompletionLogEntity] — append-only audit of every state change, including undo. Undo
 *   restores the previous status and writes its own row rather than deleting history.
 *
 * All four tables are additions; no existing column is renamed, retyped or dropped.
 */

@Entity(tableName = "habit_rules")
data class HabitRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val domain: String = "",
    /** daily | weekdays | weekly */
    val cadence: String = "daily",
    /** ISO weekdays, 1 = Monday … 7 = Sunday, comma separated. Used by weekdays/weekly. */
    val weekdays: String = "",
    val windowStartMin: Int? = null,
    val windowEndMin: Int? = null,
    val weeklyTarget: Int? = null,
    /** Optional Minimum Version: what "good enough on a bad day" means for this habit. */
    val minimumLabel: String? = null,
    val paused: Boolean = false,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_tasks")
data class ChecklistTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val domain: String = "",
    /** Date-only deadline, `yyyy-MM-dd`. */
    val dueDate: String,
    val dueTimeMin: Int? = null,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_occurrences", indices = [Index("dateKey")])
data class ChecklistOccurrenceEntity(
    @PrimaryKey override val id: String,
    override val dateKey: String,
    /** habit | task | carried */
    val kind: String,
    val sourceId: String,
    /** Snapshot, so editing or archiving a rule never rewrites history. */
    override val title: String,
    val domain: String = "",
    val windowStartMin: Int? = null,
    val windowEndMin: Int? = null,
    val minimumLabel: String? = null,
    /** pending | full | minimum | later | skipped | carried | scheduled | dropped */
    override val status: String = "pending",
    val snoozeUntil: Long? = null,
    val generatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : ChecklistOccurrenceish

@Entity(tableName = "completion_logs", indices = [Index("occurrenceId")])
data class CompletionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurrenceId: String,
    val dateKey: String,
    /** full | minimum | later | skipped | undo */
    val action: String,
    val fromStatus: String,
    val toStatus: String,
    /** Optional evidence — never required. */
    val minutes: Int? = null,
    val quantity: Int? = null,
    val topic: String = "",
    val note: String = "",
    val at: Long = System.currentTimeMillis()
)

/**
 * Phase 9 — title-first capture that has not been organised yet.
 *
 * Kept as its own table rather than a nullable `dueDate` on `checklist_tasks`: making an
 * existing `NOT NULL` column nullable would mean recreating that table inside a migration,
 * and every migration in this project is a pure addition. Scheduling an inbox item is an
 * explicit owner action that creates a task and removes the inbox row.
 */
@Entity(tableName = "inbox_items")
data class InboxItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val domain: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Status/action vocabulary, shared by the repository, the UI and the tests. */
object ChecklistValues {
    const val KIND_HABIT = "habit"
    const val KIND_TASK = "task"

    const val STATUS_PENDING = "pending"
    const val STATUS_FULL = "full"
    const val STATUS_MINIMUM = "minimum"
    const val STATUS_LATER = "later"
    const val STATUS_SKIPPED = "skipped"

    // Phase 9 leftover outcomes. All three are settled for the day and all three are
    // explicitly *not* completions (DL7, DL9).
    /** Moved to another date; a `carried:` occurrence now holds it. */
    const val STATUS_CARRIED = "carried"
    /** Given a new date deliberately; the target occurrence holds it. */
    const val STATUS_SCHEDULED = "scheduled"
    /** Dropped on purpose. Never silently discarded — this is a recorded decision. */
    const val STATUS_DROPPED = "dropped"

    const val ACTION_FULL = "full"
    const val ACTION_MINIMUM = "minimum"
    const val ACTION_LATER = "later"
    const val ACTION_SKIP = "skipped"
    const val ACTION_UNDO = "undo"

    // Phase 9 actions, written to the same append-only log.
    const val ACTION_MOVE = "move"
    const val ACTION_SCHEDULE = "schedule"
    const val ACTION_DROP = "drop"

    /** Statuses that are settled for the day; skipped is settled but is not a completion. */
    fun isClosed(status: String): Boolean =
        status == STATUS_FULL || status == STATUS_MINIMUM || status == STATUS_SKIPPED ||
            status == STATUS_CARRIED || status == STATUS_SCHEDULED || status == STATUS_DROPPED

    /** Closed by a day-close decision rather than by doing the work. */
    fun isLeftoverOutcome(status: String): Boolean =
        status == STATUS_CARRIED || status == STATUS_SCHEDULED || status == STATUS_DROPPED

    fun isCompleted(status: String): Boolean =
        status == STATUS_FULL || status == STATUS_MINIMUM
}
