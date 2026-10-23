package io.github.pabi277.dayledger.reminders

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Phase 10 — schema v8.
 *
 * The requirements are explicit that reminder state lives **per dated occurrence**, not
 * only as a transient alarm: "store a stable occurrence identity and fire sequence". These
 * two tables are that state, and both are pure additions — no existing column is renamed,
 * retyped or dropped, so no owner row is rewritten on upgrade.
 *
 * - [ReminderStateEntity] holds the *current* state of one occurrence: which profile it
 *   follows, when it is next due, and how many of its fires were audible. Its primary key
 *   **is** the occurrence id, so one occurrence can only ever have one state row.
 * - [ReminderEventEntity] is append-only: every fire, every notification action and every
 *   ignored stale tap. The daily Core/general caps are counted from this table rather than
 *   from a mutable counter, so a cap cannot be "lost" by a race between two alarms and the
 *   owner can always see why a reminder did or did not arrive.
 */
@Entity(tableName = "reminder_states")
data class ReminderStateEntity(
    /** The occurrence this state belongs to (`kind:source:date`). */
    @PrimaryKey val occurrenceId: String,
    /** core | standard | flexible */
    val profile: String = ReminderProfile.STANDARD.key,
    /** When this occurrence is next due, or `null` when nothing is scheduled. */
    val nextFireAt: Long? = null,
    /** Sound/vibration fires used by this occurrence. */
    val audibleFires: Int = 0,
    /** All fires, audible or silent, for this occurrence. */
    val totalFires: Int = 0,
    val lastFireAt: Long? = null,
    /**
     * Set when the last scheduling attempt could not use an exact alarm, so the UI can say
     * "Android timing is approximate" instead of pretending the reminder is punctual.
     */
    val degraded: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reminder_events",
    indices = [Index("occurrenceId"), Index("dateKey")]
)
data class ReminderEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurrenceId: String,
    val dateKey: String,
    /** fire | action | stale | stopped | plan | degraded */
    val kind: String,
    val profile: String = "",
    val audible: Boolean = false,
    /** Free text: the action taken, or the reason a fire was withheld. */
    val detail: String = "",
    val at: Long = System.currentTimeMillis()
)

/** Vocabulary for [ReminderEventEntity.kind]. */
object ReminderEventKind {
    /** An alert was posted; [ReminderEventEntity.audible] says whether it made a sound. */
    const val FIRE = "fire"

    /** The owner tapped Done / Minimum / Later / Skip on a notification. */
    const val ACTION = "action"

    /** A tap arrived for an occurrence that was already settled. Nothing was written. */
    const val STALE = "stale"

    /** The occurrence will not fire again (budget used, day over, closed). */
    const val STOPPED = "stopped"

    /** A schedule decision, recorded so "why didn't it ring?" has an answer. */
    const val PLAN = "plan"

    /** Android would not give an exact alarm. */
    const val DEGRADED = "degraded"
}
