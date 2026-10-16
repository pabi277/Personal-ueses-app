package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface EarningDao {
    @Upsert
    suspend fun insert(e: EarningEntryEntity): Long

    @Query("SELECT * FROM earnings ORDER BY loggedAt DESC")
    suspend fun all(): List<EarningEntryEntity>

    @Query("SELECT * FROM earnings WHERE dateKey = :dateKey")
    suspend fun forDate(dateKey: String): List<EarningEntryEntity>

    @Query("SELECT COALESCE(SUM(amountPaise),0) FROM earnings WHERE dateKey = :dateKey")
    suspend fun totalForDate(dateKey: String): Int
}
