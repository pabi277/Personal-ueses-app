package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "template_items")
data class TemplateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: String,
    val startMin: Int,
    val endMin: Int,
    val title: String
)
