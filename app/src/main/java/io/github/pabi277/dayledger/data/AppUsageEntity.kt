package io.github.pabi277.dayledger.data

import androidx.room.Entity

@Entity(tableName = "app_usage", primaryKeys = ["packageName", "dateKey"])
data class AppUsageEntity(
    val packageName: String,
    val dateKey: String,
    val foregroundMinutes: Int,
    val pickups: Int,
    val longestSessionMin: Int,
    val source: String = "device",
    val updatedAt: Long = System.currentTimeMillis()
)
