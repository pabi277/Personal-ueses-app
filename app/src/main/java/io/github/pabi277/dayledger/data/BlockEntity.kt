package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocks")
data class BlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val startMin: Int,
    val endMin: Int,
    val title: String,
    val kind: String = "soft",
    val reminder: Boolean = false
)
