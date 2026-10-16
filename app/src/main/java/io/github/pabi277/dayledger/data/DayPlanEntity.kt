package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_plans")
data class DayPlanEntity(
    @PrimaryKey val dateKey: String,
    val templateId: String? = null,
    val note: String = ""
)
