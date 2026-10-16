package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SettingDao {
    @Upsert
    suspend fun upsert(s: SettingEntity)

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun get(key: String): String?

    @Query("SELECT * FROM settings")
    suspend fun all(): List<SettingEntity>
}
