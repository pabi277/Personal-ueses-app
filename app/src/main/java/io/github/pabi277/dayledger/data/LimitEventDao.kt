package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LimitEventDao {
    @Upsert
    suspend fun insert(e: LimitEventEntity): Long

    @Query("SELECT * FROM limit_events WHERE dateKey = :dateKey ORDER BY reachedAt DESC")
    suspend fun forDate(dateKey: String): List<LimitEventEntity>
}
