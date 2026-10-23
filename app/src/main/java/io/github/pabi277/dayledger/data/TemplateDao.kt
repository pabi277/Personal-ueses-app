package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface TemplateDao {
    @Upsert
    suspend fun upsertTemplate(t: TemplateEntity)

    @Upsert
    suspend fun upsertItem(i: TemplateItemEntity)

    @Query("SELECT * FROM templates ORDER BY name")
    suspend fun templates(): List<TemplateEntity>

    @Query("SELECT * FROM template_items WHERE templateId = :templateId ORDER BY startMin")
    suspend fun items(templateId: String): List<TemplateItemEntity>

    @Query("UPDATE templates SET lastUsedAt = :ts WHERE id = :id")
    suspend fun touch(id: String, ts: Long)

    @Query("SELECT templateId, COUNT(*) AS itemCount FROM template_items GROUP BY templateId")
    suspend fun itemCounts(): List<TemplateItemCount>

    @Query("DELETE FROM template_items WHERE templateId = :id")
    suspend fun deleteItems(id: String)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}

data class TemplateItemCount(val templateId: String, val itemCount: Int)
