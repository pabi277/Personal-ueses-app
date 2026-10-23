package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppUsageDao {
    @Upsert
    suspend fun upsertAll(items: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE dateKey = :dateKey ORDER BY foregroundMinutes DESC")
    suspend fun forDate(dateKey: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE dateKey BETWEEN :start AND :end")
    suspend fun between(start: String, end: String): List<AppUsageEntity>

    @Query("SELECT COALESCE(SUM(foregroundMinutes),0) FROM app_usage WHERE dateKey = :dateKey")
    suspend fun totalForDate(dateKey: String): Int

    /**
     * Ingest is now a replace, not an accumulate: a day's rows are deleted and rewritten
     * from the same clipped timeline. That matters after the 2026-10-23 measurement fix,
     * where the old rows held inflated `totalTimeInForeground` values (and rows for the
     * launcher and dialer) that an upsert alone would never have corrected.
     */
    @Query("DELETE FROM app_usage WHERE dateKey = :dateKey")
    suspend fun deleteForDate(dateKey: String)

    /** Every stored usage row, for the CSV export (newest day first). */
    @Query("SELECT * FROM app_usage ORDER BY dateKey DESC, foregroundMinutes DESC")
    suspend fun allRows(): List<AppUsageEntity>
}
