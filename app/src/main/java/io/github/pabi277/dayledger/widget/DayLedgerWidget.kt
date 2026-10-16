package io.github.pabi277.dayledger.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.text.Text
import io.github.pabi277.dayledger.data.DayLedgerDatabase
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DayLedgerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = DayLedgerDatabase.getInstance(context)
        val today = LocalDate.now().toString()
        val total = db.usageDao().totalForDate(today)
        val apps = db.usageDao().forDate(today)
        val info = db.appInfoDao().all().associateBy { it.packageName }
        val topWaste = apps.filter { info[it.packageName]?.category == "Social/Waste" }
            .maxByOrNull { it.foregroundMinutes }
        val topWasteLabel = topWaste?.let { info[it.packageName]?.label ?: it.packageName } ?: "—"
        val asOf = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        provideContent {
            GlanceTheme {
                Column {
                    Text("Day Ledger")
                    Text("Total: ${total / 60}h ${total % 60}m")
                    Text("Top waste: $topWasteLabel")
                    Text("as of $asOf")
                }
            }
        }
    }
}
