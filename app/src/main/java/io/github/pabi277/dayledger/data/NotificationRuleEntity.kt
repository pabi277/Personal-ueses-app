package io.github.pabi277.dayledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_rules")
data class NotificationRuleEntity(
    @PrimaryKey val id: String,
    val type: String, // scheduled | threshold | digest
    val enabled: Boolean = true,
    val times: String = "21:00", // comma separated HH:mm
    val days: String = "0,1,2,3,4,5,6", // 0=Sun
    val scope: String = "all", // category/app scope
    val tone: String = "gentle",
    val templateText: String = "Today: {total} 🌙 Waste {waste}m. Earning done ₹{earned}.",
    val quietFrom: String = "23:30",
    val quietTo: String = "07:00",
    val dailyCap: Int = 6
)
