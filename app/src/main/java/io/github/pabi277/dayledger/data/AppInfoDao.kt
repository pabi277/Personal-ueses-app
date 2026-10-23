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

    /**
     * Refresh a label that was stored as a stand-in. Rows created before the label fix
     * carry the package name twice (`label == packageName`), which is why the owner's
     * 2026-10-23 screenshot still showed `com.android.chrome` on the Today screen.
     * Only placeholders are touched — a real label is never overwritten.
     */
    @Query(
        "UPDATE app_info SET label = :label WHERE packageName = :packageName AND label = :packageName"
    )
    suspend fun updatePlaceholderLabel(packageName: String, label: String)

    @Query("UPDATE app_info SET category = :category WHERE packageName = :packageName")
    suspend fun updateCategory(packageName: String, category: String)

    // `alwaysAllowed` is INTEGER NOT NULL with no SQL default, so omitting it made
    // every row violate the constraint and INSERT OR IGNORE dropped it silently —
    // app_info stayed empty while app_usage filled up (found in the owner's
    // 2026-10-23 backup: 0 app_info rows against 301 app_usage rows).
    @Query(
        "INSERT OR IGNORE INTO app_info (packageName, label, category, alwaysAllowed, limitMin) " +
            "VALUES (:packageName, :label, 'Unclassified', 0, NULL)"
    )
    suspend fun insertIfMissing(packageName: String, label: String)
}
