package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "limit_events")
data class LimitEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val packageName: String,
    val limitMin: Int,
    val reachedAt: Long,
    val action: String, // block | nudge
    val blockDurationMin: Int = 0,
    val unlockedAt: Long = 0,
    val overrideType: String = "none" // none | extend | pin
)
