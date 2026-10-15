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
}
