package io.github.pabi277.dayledger.data.checklist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HabitRuleDao {
    @Upsert
    suspend fun upsert(rule: HabitRuleEntity)

    @Query("SELECT * FROM habit_rules ORDER BY archived, paused, title")
    suspend fun all(): List<HabitRuleEntity>

    /** Only rules that can generate new occurrences. */
    @Query("SELECT * FROM habit_rules WHERE archived = 0 AND paused = 0 ORDER BY title")
    suspend fun active(): List<HabitRuleEntity>

    @Query("DELETE FROM habit_rules WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ChecklistTaskDao {
    @Upsert
    suspend fun upsert(task: ChecklistTaskEntity)

    @Query("SELECT * FROM checklist_tasks WHERE archived = 0 ORDER BY dueDate, title")
    suspend fun active(): List<ChecklistTaskEntity>

    @Query("SELECT * FROM checklist_tasks WHERE archived = 0 AND dueDate = :dateKey ORDER BY dueTimeMin, title")
    suspend fun forDate(dateKey: String): List<ChecklistTaskEntity>
}

@Dao
interface ChecklistOccurrenceDao {
    /**
     * `INSERT OR IGNORE` on a stable primary key is the idempotency guarantee: generating
     * the same date twice can only ever find the row already there.
     */
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(rows: List<ChecklistOccurrenceEntity>)

    @Query("SELECT * FROM checklist_occurrences WHERE dateKey = :dateKey ORDER BY title")
    suspend fun forDate(dateKey: String): List<ChecklistOccurrenceEntity>

    @Query("SELECT * FROM checklist_occurrences WHERE id = :id")
    suspend fun get(id: String): ChecklistOccurrenceEntity?

    @Query("UPDATE checklist_occurrences SET status = :status, snoozeUntil = :snoozeUntil, updatedAt = :at WHERE id = :id")
    suspend fun setStatus(id: String, status: String, snoozeUntil: Long?, at: Long)

    @Query("SELECT COUNT(*) FROM checklist_occurrences WHERE dateKey = :dateKey")
    suspend fun countForDate(dateKey: String): Int

    @Query("SELECT * FROM checklist_occurrences ORDER BY dateKey DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ChecklistOccurrenceEntity>
}

@Dao
interface InboxDao {
    @Upsert
    suspend fun upsert(item: InboxItemEntity)

    @Query("SELECT * FROM inbox_items ORDER BY createdAt")
    suspend fun all(): List<InboxItemEntity>

    @Query("SELECT * FROM inbox_items WHERE id = :id")
    suspend fun get(id: String): InboxItemEntity?

    @Query("DELETE FROM inbox_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM inbox_items")
    suspend fun count(): Int
}

@Dao
interface CompletionLogDao {
    @Insert
    suspend fun append(log: CompletionLogEntity): Long

    @Query("SELECT * FROM completion_logs WHERE occurrenceId = :occurrenceId ORDER BY at DESC, id DESC")
    suspend fun forOccurrence(occurrenceId: String): List<CompletionLogEntity>

    @Query("SELECT * FROM completion_logs WHERE dateKey = :dateKey ORDER BY at DESC, id DESC")
    suspend fun forDate(dateKey: String): List<CompletionLogEntity>

    @Query("SELECT * FROM completion_logs ORDER BY at DESC, id DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<CompletionLogEntity>

    /** Every carry/schedule decision, for the carryover counter. */
    @Query("SELECT * FROM completion_logs WHERE action IN ('move', 'schedule') ORDER BY at")
    suspend fun carryoverLogs(): List<CompletionLogEntity>

    @Query("SELECT * FROM completion_logs WHERE occurrenceId = :occurrenceId AND action IN ('move', 'schedule')")
    suspend fun carryoverLogsFor(occurrenceId: String): List<CompletionLogEntity>
}
