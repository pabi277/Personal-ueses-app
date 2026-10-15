package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: String, // ceiling | floor
    val budgetMin: Int,
    val color: Long
)
