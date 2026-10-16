package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NotificationRuleDao {
    @Upsert
    suspend fun upsertAll(items: List<NotificationRuleEntity>)

    @Upsert
    suspend fun upsert(item: NotificationRuleEntity)

    @Query("SELECT * FROM notification_rules ORDER BY id")
    suspend fun all(): List<NotificationRuleEntity>

    @Query("SELECT * FROM notification_rules WHERE id = :id")
    suspend fun get(id: String): NotificationRuleEntity?

    @Query("UPDATE notification_rules SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE notification_rules SET templateText = :text WHERE id = :id")
    suspend fun updateText(id: String, text: String)

    @Query("UPDATE notification_rules SET times = :times, quietFrom = :qf, quietTo = :qt WHERE id = :id")
    suspend fun updateSchedule(id: String, times: String, qf: String, qt: String)
}
