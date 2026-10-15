package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface LearningDao {
    @Upsert
    suspend fun insert(e: LearningEntryEntity): Long

    @Query("SELECT * FROM learning ORDER BY createdAt DESC")
    suspend fun all(): List<LearningEntryEntity>

    @Query("SELECT * FROM learning WHERE dateKey = :dateKey")
    suspend fun forDate(dateKey: String): List<LearningEntryEntity>

    @Query("SELECT DISTINCT dateKey FROM learning ORDER BY dateKey DESC")
    suspend fun daysWithLearning(): List<String>
}
