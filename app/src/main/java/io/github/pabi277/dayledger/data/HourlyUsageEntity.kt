package io.github.pabi277.dayledger.data

import androidx.room.Entity

@Entity(tableName = "hourly_usage", primaryKeys = ["dateKey", "hour"])
data class HourlyUsageEntity(
    val dateKey: String,
    val hour: Int,
    val minutes: Int
)
