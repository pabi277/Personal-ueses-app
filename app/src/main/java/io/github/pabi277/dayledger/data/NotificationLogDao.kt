package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NotificationLogDao {
    @Upsert
    suspend fun insert(log: NotificationLogEntity)

    @Query("SELECT COUNT(*) FROM notification_log WHERE dateKey = :dateKey")
    suspend fun countForDate(dateKey: String): Int

    @Query("SELECT COUNT(*) FROM notification_log WHERE ruleId = :ruleId AND dateKey = :dateKey")
    suspend fun countForRuleDate(ruleId: String, dateKey: String): Int
}
