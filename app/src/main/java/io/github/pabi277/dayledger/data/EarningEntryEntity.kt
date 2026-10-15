package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earnings")
data class EarningEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val source: String,
    val taskType: String,
    val appId: String? = null,
    val amountPaise: Int,
    val loggedAt: Long = System.currentTimeMillis(),
    val attributedMinutes: Int = 0
)
