package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HourlyUsageDao {
    @Upsert
    suspend fun upsertAll(items: List<HourlyUsageEntity>)

    @Query("SELECT * FROM hourly_usage WHERE dateKey = :dateKey ORDER BY hour")
    suspend fun forDate(dateKey: String): List<HourlyUsageEntity>
}
