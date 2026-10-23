package io.github.pabi277.dayledger.reminders

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ReminderStateDao {

    @Upsert
    suspend fun upsert(state: ReminderStateEntity)

    @Query("SELECT * FROM reminder_states WHERE occurrenceId = :occurrenceId")
    suspend fun get(occurrenceId: String): ReminderStateEntity?

    @Query("SELECT * FROM reminder_states")
    suspend fun all(): List<ReminderStateEntity>

    @Query(
        "UPDATE reminder_states SET profile = :profile, updatedAt = :at " +
            "WHERE occurrenceId = :occurrenceId"
    )
    suspend fun setProfile(occurrenceId: String, profile: String, at: Long)

    @Query("UPDATE reminder_states SET nextFireAt = :nextFireAt WHERE occurrenceId = :occurrenceId")
    suspend fun setNextFire(occurrenceId: String, nextFireAt: Long?)

    @Query("UPDATE reminder_states SET degraded = :degraded, updatedAt = :at WHERE occurrenceId = :occurrenceId")
    suspend fun setDegraded(occurrenceId: String, degraded: String, at: Long)

    /** The soonest scheduled fire across every tracked occurrence, for the health panel. */
    @Query("SELECT MIN(nextFireAt) FROM reminder_states WHERE nextFireAt IS NOT NULL")
    suspend fun soonest(): Long?
}

@Dao
interface ReminderEventDao {

    @Insert
    suspend fun append(event: ReminderEventEntity): Long

    /**
     * Audible Core fires today. Counted from the append-only log rather than a counter so
     * two alarms racing at the same moment cannot both "win" a stale reading of the cap.
     */
    @Query(
        "SELECT COUNT(*) FROM reminder_events WHERE dateKey = :dateKey " +
            "AND kind = 'fire' AND audible = 1 AND profile = 'core'"
    )
    suspend fun audibleCoreFires(dateKey: String): Int

    @Query(
        "SELECT COUNT(*) FROM reminder_events WHERE dateKey = :dateKey " +
            "AND kind = 'fire' AND audible = 1 AND profile <> 'core'"
    )
    suspend fun audibleGeneralFires(dateKey: String): Int

    @Query("SELECT * FROM reminder_events WHERE occurrenceId = :occurrenceId ORDER BY at DESC, id DESC")
    suspend fun forOccurrence(occurrenceId: String): List<ReminderEventEntity>

    @Query("SELECT * FROM reminder_events WHERE dateKey = :dateKey ORDER BY at DESC, id DESC")
    suspend fun forDate(dateKey: String): List<ReminderEventEntity>

    @Query("SELECT COUNT(*) FROM reminder_events WHERE occurrenceId = :occurrenceId AND kind = 'action'")
    suspend fun actionCount(occurrenceId: String): Int
}
