package io.github.pabi277.dayledger.data.checklist

import androidx.room.withTransaction
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import java.time.LocalDate

/**
 * Database side of the Phase 8 checklist.
 *
 * Two guarantees worth stating explicitly, because both are Phase 8 "Done means" clauses:
 *
 * - **Generation is idempotent.** Ids come from [ChecklistGeneration.occurrenceId] and the
 *   insert ignores conflicts, so opening the app, refreshing, restarting or rebooting can
 *   only find the rows already there.
 * - **Every state change is logged.** [act] and [undo] append a [CompletionLogEntity]
 *   inside the same transaction as the status write, so a second tap cannot produce a
 *   second completion and an undo never deletes its own audit trail.
 */
class ChecklistRepository(private val db: DayLedgerDatabase) {

    private val habitDao = db.habitRuleDao()
    private val taskDao = db.checklistTaskDao()
    private val occurrenceDao = db.checklistOccurrenceDao()
    private val logDao = db.completionLogDao()

    /** Generate the given days, then return them. Safe to call on every resume. */
    suspend fun ensureGenerated(dates: List<LocalDate>): Unit = db.withTransaction {
        val habits = habitDao.active().map { it.toSpec() }
        val tasks = taskDao.active().mapNotNull { it.toSpecOrNull() }
        val now = System.currentTimeMillis()
        for (date in dates) {
            val drafts = ChecklistGeneration.draftsFor(date, habits, tasks)
            if (drafts.isEmpty()) continue
            occurrenceDao.insertIfMissing(
                drafts.map { draft ->
                    ChecklistOccurrenceEntity(
                        id = draft.id,
                        dateKey = draft.dateKey,
                        kind = draft.kind,
                        sourceId = draft.sourceId,
                        title = draft.title,
                        domain = draft.domain,
                        windowStartMin = draft.windowStartMin,
                        windowEndMin = draft.windowEndMin,
                        minimumLabel = draft.minimumLabel,
                        status = ChecklistValues.STATUS_PENDING,
                        generatedAt = now,
                        updatedAt = now
                    )
                }
            )
        }
    }

    /** Today plus the next [days] days, so tomorrow is ready without a second mechanism. */
    suspend fun ensureGeneratedFromToday(days: Int = 7) {
        val today = LocalDate.now()
        ensureGenerated((0..days).map { today.plusDays(it.toLong()) })
    }

    suspend fun forDate(dateKey: String): List<ChecklistOccurrenceEntity> =
        occurrenceDao.forDate(dateKey)

    suspend fun recentLogs(dateKey: String): List<CompletionLogEntity> = logDao.forDate(dateKey)

    // ---------------------------------------------------------------- actions

    /**
     * One tap, no form. Returns the new status, or `null` if the occurrence is gone.
     *
     * A tap on an already-settled item does not write a second completion: the request is
     * ignored and the existing state stands.
     */
    suspend fun act(
        occurrenceId: String,
        action: String,
        minutes: Int? = null,
        quantity: Int? = null,
        topic: String = "",
        note: String = ""
    ): String? = db.withTransaction {
        val occurrence = occurrenceDao.get(occurrenceId) ?: return@withTransaction null
        if (ChecklistValues.isClosed(occurrence.status)) return@withTransaction occurrence.status
        val toStatus = when (action) {
            ChecklistValues.ACTION_FULL -> ChecklistValues.STATUS_FULL
            ChecklistValues.ACTION_MINIMUM -> ChecklistValues.STATUS_MINIMUM
            ChecklistValues.ACTION_LATER -> ChecklistValues.STATUS_LATER
            ChecklistValues.ACTION_SKIP -> ChecklistValues.STATUS_SKIPPED
            else -> return@withTransaction occurrence.status
        }
        val now = System.currentTimeMillis()
        val snooze = if (action == ChecklistValues.ACTION_LATER) now + LATER_MILLIS else null
        occurrenceDao.setStatus(occurrenceId, toStatus, snooze, now)
        logDao.append(
            CompletionLogEntity(
                occurrenceId = occurrenceId,
                dateKey = occurrence.dateKey,
                action = action,
                fromStatus = occurrence.status,
                toStatus = toStatus,
                minutes = minutes,
                quantity = quantity,
                topic = topic,
                note = note,
                at = now
            )
        )
        toStatus
    }

    /** Attach optional evidence to the most recent completion, without touching status. */
    suspend fun attachEvidence(
        occurrenceId: String,
        minutes: Int? = null,
        quantity: Int? = null,
        topic: String = "",
        note: String = ""
    ) = db.withTransaction {
        val occurrence = occurrenceDao.get(occurrenceId) ?: return@withTransaction
        if (!ChecklistValues.isClosed(occurrence.status)) return@withTransaction
        logDao.append(
            CompletionLogEntity(
                occurrenceId = occurrenceId,
                dateKey = occurrence.dateKey,
                action = "evidence",
                fromStatus = occurrence.status,
                toStatus = occurrence.status,
                minutes = minutes,
                quantity = quantity,
                topic = topic,
                note = note
            )
        )
    }

    /**
     * Undo the last state change: restore the status recorded as `fromStatus` and append an
     * `undo` row. History is never deleted — the completion and its undo both remain.
     */
    suspend fun undo(occurrenceId: String): String? = db.withTransaction {
        val occurrence = occurrenceDao.get(occurrenceId) ?: return@withTransaction null
        val last = logDao.forOccurrence(occurrenceId)
            .firstOrNull { it.action != ChecklistValues.ACTION_UNDO && it.action != "evidence" }
            ?: return@withTransaction occurrence.status
        val now = System.currentTimeMillis()
        occurrenceDao.setStatus(occurrenceId, last.fromStatus, null, now)
        logDao.append(
            CompletionLogEntity(
                occurrenceId = occurrenceId,
                dateKey = occurrence.dateKey,
                action = ChecklistValues.ACTION_UNDO,
                fromStatus = occurrence.status,
                toStatus = last.fromStatus,
                at = now
            )
        )
        last.fromStatus
    }

    // ---------------------------------------------------------------- editing

    suspend fun upsertHabit(rule: HabitRuleEntity) {
        habitDao.upsert(rule)
        // Editing a rule must not rewrite earlier days: only future generation uses it.
        ensureGeneratedFromToday()
    }

    suspend fun setHabitPaused(id: String, paused: Boolean) {
        val rule = habitDao.all().firstOrNull { it.id == id } ?: return
        habitDao.upsert(rule.copy(paused = paused))
    }

    suspend fun archiveHabit(id: String) {
        val rule = habitDao.all().firstOrNull { it.id == id } ?: return
        habitDao.upsert(rule.copy(archived = true))
    }

    suspend fun addTask(title: String, date: LocalDate, dueTimeMin: Int? = null, domain: String = "") {
        val task = ChecklistTaskEntity(
            id = "task-" + System.currentTimeMillis().toString(36) + "-" + (0..999).random(),
            title = title,
            domain = domain,
            dueDate = date.toString(),
            dueTimeMin = dueTimeMin
        )
        taskDao.upsert(task)
        ensureGenerated(listOf(date))
    }

    suspend fun habits(): List<HabitRuleEntity> = habitDao.all()

    suspend fun tasks(): List<ChecklistTaskEntity> = taskDao.active()

    private fun HabitRuleEntity.toSpec() = HabitSpec(
        id = id,
        title = title,
        domain = domain,
        cadence = when (cadence) {
            "weekdays" -> Cadence.WEEKDAYS
            "weekly" -> Cadence.WEEKLY
            else -> Cadence.DAILY
        },
        weekdays = ChecklistGeneration.parseWeekdays(weekdays),
        windowStartMin = windowStartMin,
        windowEndMin = windowEndMin,
        minimumLabel = minimumLabel,
        paused = paused,
        archived = archived
    )

    private fun ChecklistTaskEntity.toSpecOrNull(): TaskSpec? {
        val date = try {
            LocalDate.parse(dueDate)
        } catch (e: Exception) {
            return null
        }
        return TaskSpec(
            id = id,
            title = title,
            domain = domain,
            dueDate = date,
            dueTimeMin = dueTimeMin,
            archived = archived
        )
    }

    companion object {
        /** "Later" is a snooze, not a completion: the item returns to the list after this. */
        const val LATER_MILLIS = 30L * 60L * 1000L
    }
}
