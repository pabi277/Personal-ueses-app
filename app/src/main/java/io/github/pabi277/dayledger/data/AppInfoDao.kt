package io.github.pabi277.dayledger.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppInfoDao {
    @Upsert
    suspend fun upsertAll(items: List<AppInfoEntity>)

    @Query("SELECT * FROM app_info ORDER BY label")
    suspend fun all(): List<AppInfoEntity>

    @Query("SELECT * FROM app_info WHERE packageName = :packageName")
    suspend fun get(packageName: String): AppInfoEntity?

    @Query("UPDATE app_info SET label = :label WHERE packageName = :packageName")
    suspend fun updateLabel(packageName: String, label: String)

    @Query("UPDATE app_info SET category = :category WHERE packageName = :packageName")
    suspend fun updateCategory(packageName: String, category: String)

    @Query("INSERT OR IGNORE INTO app_info (packageName, label, category) VALUES (:packageName, :label, 'Unclassified')")
    suspend fun insertIfMissing(packageName: String, label: String)
}
