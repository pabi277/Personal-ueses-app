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
    @PrimaryKey val id: String,
    val dateKey: String,
    /** habit | task */
    val kind: String,
    val sourceId: String,
    /** Snapshot, so editing or archiving a rule never rewrites history. */
    val title: String,
    val domain: String = "",
    val windowStartMin: Int? = null,
    val windowEndMin: Int? = null,
    val minimumLabel: String? = null,
    /** pending | full | minimum | later | skipped */
    val status: String = "pending",
    val snoozeUntil: Long? = null,
    val generatedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

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

/** Status/action vocabulary, shared by the repository, the UI and the tests. */
object ChecklistValues {
    const val KIND_HABIT = "habit"
    const val KIND_TASK = "task"

    const val STATUS_PENDING = "pending"
    const val STATUS_FULL = "full"
    const val STATUS_MINIMUM = "minimum"
    const val STATUS_LATER = "later"
    const val STATUS_SKIPPED = "skipped"

    const val ACTION_FULL = "full"
    const val ACTION_MINIMUM = "minimum"
    const val ACTION_LATER = "later"
    const val ACTION_SKIP = "skipped"
    const val ACTION_UNDO = "undo"

    /** Statuses that are settled for the day; skipped is settled but is not a completion. */
    fun isClosed(status: String): Boolean =
        status == STATUS_FULL || status == STATUS_MINIMUM || status == STATUS_SKIPPED

    fun isCompleted(status: String): Boolean =
        status == STATUS_FULL || status == STATUS_MINIMUM
}
