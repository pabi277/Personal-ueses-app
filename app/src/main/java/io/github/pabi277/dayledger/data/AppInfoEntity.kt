package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val category: String = "Unclassified",
    val alwaysAllowed: Boolean = false,
    val limitMin: Int? = null
)
