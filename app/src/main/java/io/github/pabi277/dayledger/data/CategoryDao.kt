package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CategoryDao {
    @Upsert
    suspend fun upsertAll(items: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun all(): List<CategoryEntity>

    @Query("UPDATE categories SET budgetMin = :budgetMin, kind = :kind WHERE id = :id")
    suspend fun updateBudget(id: String, budgetMin: Int, kind: String)
}
